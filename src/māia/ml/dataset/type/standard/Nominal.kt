package māia.ml.dataset.type.standard

import māia.ml.dataset.type.DataRepresentation
import māia.ml.dataset.type.DataType
import māia.ml.dataset.type.EntropicRepresentation
import māia.ml.dataset.type.FiniteDataType
import māia.util.*
import māia.util.datastructure.OrderedSet
import māia.util.datastructure.buildOrderedSet
import māia.util.error.UNREACHABLE_CODE
import java.math.BigInteger

/**
 * Base-class for implementations of the canonical representation of
 * the [Nominal] data-type. This representation presents values as the [String]
 * names of the nominal classes.
 *
 * @param Self See [DataRepresentation].
 * @param N The type of [Nominal] that owns this representation.
 */
abstract class NominalCanonicalRepresentation<
        Self: NominalCanonicalRepresentation<Self, N>,
        N: Nominal<N, Self, *, *>
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
        N : Nominal<N, *, *, Self>
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
        Self: Nominal<Self, C, E, I>,
        C: NominalCanonicalRepresentation<C, Self>,
        E: EntropicRepresentation<E, Self>,
        I: NominalIndexRepresentation<I, Self>
> private constructor(
        canonicalRepresentation: C,
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
        entropicRepresentation : E,
        indexRepresentation : I,
        supportsMissingValues: Boolean,
        vararg categories : String
    ) : this(
        canonicalRepresentation,
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

    final override fun equals(other: Any?): Boolean {
        // A nominal type is equal to another if it has the same categories
        // in the same order
        return other is Nominal<*, *, *, *> && other.categories == categories
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
    ): Nominal<PlaceHolder, PlaceHolder.CanonicalRepresentation, PlaceHolder.EntropicRepresentation, PlaceHolder.IndexRepresentation>(
        CanonicalRepresentation(),
        EntropicRepresentation(),
        IndexRepresentation(),
        supportsMissingValues,
        *categories
    ) {
        override fun copy() : PlaceHolder = PlaceHolder(supportsMissingValues, *toTypedArray())

        class CanonicalRepresentation: NominalCanonicalRepresentation<CanonicalRepresentation, PlaceHolder>() {
            override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PlaceHolder, I>) : String {
                return when (fromRepresentation) {
                    is CanonicalRepresentation -> value as String
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

        class EntropicRepresentation: māia.ml.dataset.type.EntropicRepresentation<EntropicRepresentation, PlaceHolder>() {
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
