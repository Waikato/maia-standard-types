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
 * Canonical [representation][DataRepresentation] of the [Nominal]
 * [data-type][DataType]. This representation presents values as the [String]
 * names of the nominal classes.
 */
class NominalCanonicalRepresentation:
    DataRepresentation<NominalCanonicalRepresentation, Nominal, String>()
{
    override fun isValid(value : String) : Boolean = dataType.isCategory(value)
    override fun initial() : String = dataType[0]
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, Nominal, I>) : String {
        return when (fromRepresentation) {
            is NominalCanonicalRepresentation -> value as String
            is NominalProbabilitiesRepresentation -> dataType[(value as ClassProbabilities).maxIndex]
            is NominalIndexRepresentation -> dataType[value as Int]
            is NominalEntropicRepresentation -> dataType[(value as BigInteger).toInt()]
            else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
        }
    }
}

/**
 * Class-probabilities [representation][DataRepresentation] of the [Nominal]
 * [data-type][DataType]. This representation presents values as an
 * [array of probabilities][ClassProbabilities] for each nominal class.
 */
class NominalProbabilitiesRepresentation:
    DataRepresentation<NominalProbabilitiesRepresentation, Nominal, ClassProbabilities>()
{
    override fun isValid(value : ClassProbabilities) : Boolean = dataType.size == value.size
    override fun initial() : ClassProbabilities = ClassProbabilities(dataType.size)
    override fun <I> convertValue(
        value : I,
        fromRepresentation: DataRepresentation<*, Nominal, I>
    ): ClassProbabilities {
        return when (fromRepresentation) {
            is NominalCanonicalRepresentation -> dataType.oneHot(value as String)
            is NominalProbabilitiesRepresentation -> value as ClassProbabilities
            is NominalIndexRepresentation -> dataType.oneHot(value as Int)
            is NominalEntropicRepresentation -> dataType.oneHot((value as BigInteger).toInt())
            else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
        }
    }
}

/**
 * Index [representation][DataRepresentation] of the [Nominal]
 * [data-type][DataType]. This representation presents values as the [Int]
 * indices of the nominal classes in the order they were declared.
 */
class NominalIndexRepresentation:
    DataRepresentation<NominalIndexRepresentation, Nominal, Int>()
{
    override fun isValid(value : Int) : Boolean = 0 <= value && value < dataType.numCategories
    override fun initial() : Int = 0
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, Nominal, I>) : Int {
        return when (fromRepresentation) {
            is NominalCanonicalRepresentation -> dataType.indexOf(value as String)
            is NominalProbabilitiesRepresentation -> (value as ClassProbabilities).maxIndex
            is NominalIndexRepresentation -> value as Int
            is NominalEntropicRepresentation -> (value as BigInteger).toInt()
            else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
        }
    }
}

/**
 * [Entropic representation][EntropicRepresentation] of the [Nominal]
 * [data-type][DataType].
 */
class NominalEntropicRepresentation: EntropicRepresentation<NominalEntropicRepresentation, Nominal>() {
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, Nominal, I>) : BigInteger {
        return when (fromRepresentation) {
            is NominalCanonicalRepresentation -> dataType.indexOf(value as String).toBigInteger()
            is NominalProbabilitiesRepresentation -> (value as ClassProbabilities).maxIndex.toBigInteger()
            is NominalIndexRepresentation -> (value as Int).toBigInteger()
            is NominalEntropicRepresentation -> value as BigInteger
            else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
        }
    }
}

/**
 * Implementation of nominal [data-types][DataType], where
 * values must be in one of a limited number of categories.
 *
 * @param supportsMissingValues
 *          See [DataType.supportsMissingValues].
 * @param categories
 *          The categories in this nominal type.
 */
class Nominal private constructor(
        supportsMissingValues: Boolean,
        private val categories : OrderedSet<String>
):
    FiniteDataType<Nominal, NominalCanonicalRepresentation, NominalEntropicRepresentation>(
        NominalCanonicalRepresentation(),
        NominalEntropicRepresentation(),
        supportsMissingValues,
        categories.size.toBigInteger()
    ),
    OrderedSet<String> by categories
{
    constructor(
        supportsMissingValues: Boolean,
        vararg categories : String
    ) : this(
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
    val indexRepresentation by NominalIndexRepresentation()

    /** Represents each value as the string label of a category. */
    val probabilitiesRepresentation by NominalProbabilitiesRepresentation()

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

        override fun toString() : String {
            return zip(this@Nominal, inner).joinToString(prefix = "[", postfix = "]") { (label, probability) ->
                "$label: $probability"
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        // A nominal type is equal to another if it has the same categories
        // in the same order
        return other is Nominal && other.categories == categories
    }

    override fun toString() : String {
        return iterator().joinToString(
                separator = "', '",
                prefix = "Nominal('",
                postfix = "')"
        )
    }

    override fun hashCode(): Int {
        return categories.hashCode()
    }

    override fun copy() : Nominal = Nominal(supportsMissingValues, *toTypedArray())

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

    override fun toString() : String {
        return iterator().enumerate().joinToString(prefix = "[", postfix = "]") { (index, probability) ->
            "$index: $probability"
        }
    }
}
