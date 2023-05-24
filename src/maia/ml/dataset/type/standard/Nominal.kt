package maia.ml.dataset.type.standard

import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.DataType
import maia.ml.dataset.type.EntropicRepresentation
import maia.ml.dataset.type.FiniteDataType
import maia.ml.dataset.type.standard.ClassProbabilities.Companion.oneHot
import maia.util.*
import maia.util.datastructure.OrderedSet
import maia.util.datastructure.buildOrderedSet
import maia.util.error.UNREACHABLE_CODE
import java.math.BigInteger

/**
 * Base-class for implementations of the canonical representation of
 * the [Nominal] data-type. This representation presents values as an array of
 * probabilities for each nominal class.
 *
 * @param Self See [DataRepresentation].
 * @param N The type of [Nominal] that owns this representation.
 */
abstract class NominalCanonicalRepresentation<
        Self: NominalCanonicalRepresentation<Self, N>,
        N: Nominal<N, Self, *, *, *>
> : DataRepresentation<Self, N, ClassProbabilities>() {
    final override fun isValid(value : ClassProbabilities) : Boolean = dataType.size == value.size
    final override fun initial() : ClassProbabilities = ClassProbabilities(dataType.size)
}

/**
 * Base-class for implementations of the label representation of
 * the [Nominal] data-type. This representation presents values as the [String]
 * names of the nominal classes.
 *
 * @param Self See [DataRepresentation].
 * @param N The type of [Nominal] that owns this representation.
 */
abstract class NominalLabelRepresentation<
        Self: NominalLabelRepresentation<Self, N>,
        N: Nominal<N, *, Self, *, *>
> : DataRepresentation<Self, N, String>() {
    final override fun isValid(value : String) : Boolean = dataType.isCategory(value)
    final override fun initial() : String = dataType[0]
}

/**
 * Base-class for implementations of the index representation of
 * the [Nominal] data-type. This representation presents values as the [Int]
 * indices of the nominal classes in the order they were declared.
 *
 * @param Self See [DataRepresentation].
 * @param N The type of [Nominal] that owns this representation.
 */
abstract class NominalIndexRepresentation<
        Self: NominalIndexRepresentation<Self, N>,
        N : Nominal<N, *, *, *, Self>
> : DataRepresentation<Self, N, Int>() {
    final override fun isValid(value : Int) : Boolean = 0 <= value && value < dataType.numCategories
    final override fun initial() : Int = 0
}

/**
 * Base-class for implementations of nominal data-types, where
 * values must be one of a number of nominal classes.
 *
 * @param canonicalRepresentation See [DataType].
 * @param entropicRepresentation See [FiniteDataType].
 * @param indexRepresentation The index representation to use for this instance.
 * @param supportsMissingValues See [DataType].
 * @param categories    The categories in this nominal type.
 *
 * @param Self See [DataType].
 * @param C See [DataType].
 * @param E See [FiniteDataType].
 * @param I The type of index representation this data-type uses.
 */
abstract class Nominal<
        Self: Nominal<Self, C, L, E, I>,
        C: NominalCanonicalRepresentation<C, Self>,
        L: NominalLabelRepresentation<L, Self>,
        E: EntropicRepresentation<E, Self>,
        I: NominalIndexRepresentation<I, Self>
> private constructor(
        canonicalRepresentation: C,
        labelRepresentation: L,
        entropicRepresentation : E,
        indexRepresentation : I,
        supportsMissingValues: Boolean,
        private val categories : OrderedSet<String>
) : FiniteDataType<Self, C, E>(
    canonicalRepresentation,
    entropicRepresentation,
    supportsMissingValues,
    categories.size.toBigInteger()
), OrderedSet<String> by categories {

    constructor(
        canonicalRepresentation: C,
        labelRepresentation : L,
        entropicRepresentation : E,
        indexRepresentation : I,
        supportsMissingValues: Boolean,
        vararg categories : String
    ) : this(
        canonicalRepresentation,
        labelRepresentation,
        entropicRepresentation,
        indexRepresentation,
        supportsMissingValues,
        buildOrderedSet { addAll(categories) }
    ) {
        // Make sure there are at least two categories
        if (categories.size < 2)
            throw IllegalArgumentException("Must provide at least 2 categories for a nominal data-type (received ${categories.size}")

        // Make sure the provided categories are unique
        val duplicateCategories = duplicates(categories.iterator())
        if (duplicateCategories.isNotEmpty())
            throw IllegalArgumentException("Duplicate categories provided: ${duplicateCategories.joinToString()}")
    }

    /** Represents each value as an index into the list of categories. */
    val indexRepresentation by indexRepresentation

    /** Represents each value as the string label of a category. */
    val labelRepresentation by labelRepresentation

    /** The number of nominal categories. */
    val numCategories : Int
        get() = size

    /** The range of valid indices for the categories. */
    val categoryIndices: IntRange = 0 until categories.size

    /**
     * Whether the given string is a category of this nominal type.
     *
     * @param category  The string to test for.
     * @return          True if the string is a category, false if not.
     */
    fun isCategory(category : String) : Boolean {
        return category in this
    }

    fun oneHot(hotIndex: Int): maia.ml.dataset.type.standard.ClassProbabilities {
        return oneHot(this@Nominal.size, hotIndex)
    }

    fun oneHot(hotLabel: String): maia.ml.dataset.type.standard.ClassProbabilities {
        val labelIndex = indexOf(hotLabel)
        if (labelIndex == -1) throw IllegalArgumentException("Unknown label '$hotLabel'")
        return oneHot(labelIndex)
    }

    inner class ClassProbabilities(
        val inner: maia.ml.dataset.type.standard.ClassProbabilities
    ): List<Double> by inner {

        init {
            if (inner.size != this@Nominal.size) {
                throw IllegalArgumentException(
                    "Nominal type has ${this@Nominal.size} categories but received ${inner.size} probabilities"
                )
            }
        }

        constructor(
            vararg probabilities : Double
        ): this(
            ClassProbabilities(this@Nominal.size, probabilities = probabilities)
        )

        constructor(label: String): this(this@Nominal.oneHot(label))

        operator fun get(label: String): Double {
            val labelIndex = this@Nominal.indexOf(label)
            if (labelIndex == -1) throw IllegalArgumentException("Unknown label '$label'")
            return inner[labelIndex]
        }

        /** The index of the class with the highest probability. */
        val maxIndex: Int get() = inner.maxIndex
    }

    final override fun equals(other: Any?): Boolean {
        // A nominal type is equal to another if it has the same categories
        // in the same order
        return other is Nominal<*, *, *, *, *> && other.categories == categories
    }

    final override fun toString() : String {
        return iterator().joinToString(
                separator = "', '",
                prefix = "Nominal('",
                postfix = "')"
        )
    }

    final override fun hashCode(): Int {
        return categories.hashCode()
    }

    /**
     * Placeholder-implementation of [Nominal].
     */
    class PlaceHolder(
        supportsMissingValues: Boolean,
        vararg categories : String
    ): Nominal<
            PlaceHolder,
            PlaceHolder.CanonicalRepresentation,
            PlaceHolder.LabelRepresentation,
            PlaceHolder.EntropicRepresentation,
            PlaceHolder.IndexRepresentation
    >(
        CanonicalRepresentation(),
        LabelRepresentation(),
        EntropicRepresentation(),
        IndexRepresentation(),
        supportsMissingValues,
        *categories
    ) {
        override fun copy() : PlaceHolder = PlaceHolder(supportsMissingValues, *toTypedArray())

        class CanonicalRepresentation: NominalCanonicalRepresentation<CanonicalRepresentation, PlaceHolder>() {
            override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PlaceHolder, I>) : maia.ml.dataset.type.standard.ClassProbabilities {
                return when (fromRepresentation) {
                    is CanonicalRepresentation -> value as maia.ml.dataset.type.standard.ClassProbabilities
                    is LabelRepresentation -> dataType.oneHot(value as String)
                    is IndexRepresentation -> dataType.oneHot(value as Int)
                    is EntropicRepresentation -> dataType.oneHot((value as BigInteger).toInt())
                    else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
                }
            }
        }

        class LabelRepresentation: NominalLabelRepresentation<LabelRepresentation, PlaceHolder>() {
            override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PlaceHolder, I>) : String {
                return when (fromRepresentation) {
                    is CanonicalRepresentation -> dataType[(value as maia.ml.dataset.type.standard.ClassProbabilities).iterator().maxWithIndex().first]
                    is LabelRepresentation -> value as String
                    is IndexRepresentation -> dataType[value as Int]
                    is EntropicRepresentation -> dataType[(value as BigInteger).toInt()]
                    else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
                }
            }

        }

        class IndexRepresentation: NominalIndexRepresentation<IndexRepresentation, PlaceHolder>() {
            override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PlaceHolder, I>) : Int {
                return when (fromRepresentation) {
                    is CanonicalRepresentation -> dataType.indexOf(value as String)
                    is IndexRepresentation -> value as Int
                    is EntropicRepresentation -> (value as BigInteger).toInt()
                    else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
                }
            }
        }

        class EntropicRepresentation: maia.ml.dataset.type.EntropicRepresentation<EntropicRepresentation, PlaceHolder>() {
            override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PlaceHolder, I>) : BigInteger {
                return when (fromRepresentation) {
                    is CanonicalRepresentation -> dataType.indexOf(value as String).toBigInteger()
                    is IndexRepresentation -> (value as Int).toBigInteger()
                    is EntropicRepresentation -> value as BigInteger
                    else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
                }
            }
        }
    }

}


/**
 * The probabilities that a nominal attributes takes a given class.
 */
class ClassProbabilities private constructor(
    private val probabilities: DoubleArray
): List<Double> by probabilities.asList() {

    companion object {
        fun oneHot(numClasses : Int, hotClass: Int): ClassProbabilities {
            val probabilities = DoubleArray(numClasses)
            probabilities[hotClass] = 1.0
            return ClassProbabilities(numClasses, probabilities = probabilities)
        }
    }

    constructor(
        numClasses: Int,
        vararg probabilities: Double
    ): this(
        eval {
            // Make sure numClasses is positive
            if (numClasses < 2)
                throw IllegalArgumentException("Must provide at least 2 categories for a nominal data-type (received ${numClasses}")

            // Make sure [0, numClasses] probability values are given
            if (probabilities.size > numClasses) {
                throw IllegalArgumentException(
                    "Expected up to ${numClasses - 1} probabilities for nominal type with $numClasses categories, " +
                            "received ${probabilities.size}"
                )
            }

            // Make sure no probability is less than zero or more than 1 (and none are NaN)
            if (probabilities.any { it.isNaN() || it < 0 || it > 1 }) throw IllegalArgumentException("Probabilities must be in [0, 1]")

            // Calculate the total probability given
            val totalProbability = probabilities.sum()

            if (probabilities.size == numClasses) {
                // All probabilities specified, sum must be 1
                if (totalProbability != 1.0) throw IllegalArgumentException("Probabilities must add to 1.0")

                probabilities
            } else { // probabilities.size < this@Nominal.size
                // First [probabilities.size] probabilities specified, sum must be <= 1
                if (totalProbability > 1.0) throw IllegalArgumentException("Probabilities must add to at most 1.0")

                // First [probabilities.size] probabilities are those specified
                val probabilitiesExpanded = DoubleArray(numClasses).apply { copyInto(probabilities) }

                // The next category uses the remainder of the probability
                probabilitiesExpanded[probabilities.size] = 1.0 - totalProbability

                // And all remaining categories are 0.0 probability
                probabilitiesExpanded
            }
        }
    )

    /** The index of the class with the highest probability. */
    val maxIndex: Int = probabilities.iterator().maxWithIndex().first
}
