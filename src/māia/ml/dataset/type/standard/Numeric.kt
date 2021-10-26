package māia.ml.dataset.type.standard

import māia.ml.dataset.type.DataRepresentation
import māia.ml.dataset.type.DataType
import māia.ml.dataset.type.FiniteDataType

/**
 * Base-class for implementations of the canonical representation of
 * the [Numeric] data-type. This representation presents values as [Double]
 * numeric values.
 *
 * @param Self See [DataRepresentation].
 * @param D The type of [Numeric] that owns this representation.
 */
abstract class NumericCanonicalRepresentation<
        Self: NumericCanonicalRepresentation<Self, D>,
        D: Numeric<D, Self>
> :
    DataRepresentation<Self, D, Double>()
{
    final override fun isValid(value : Double) : Boolean = !value.isNaN()
    final override fun initial() : Double = 0.0
}

/**
 * Base-class for implementations of numeric data-types, where
 * values must a number.
 *
 * @param canonicalRepresentation See [DataType].
 * @param supportsMissingValues See [DataType].
 *
 * @param Self See [DataType].
 * @param C See [DataType].
 */
abstract class Numeric<
        Self: Numeric<Self, C>,
        C: NumericCanonicalRepresentation<C, Self>
>(
    canonicalRepresentation: C,
    supportsMissingValues: Boolean
) : DataType<Self, C>(
    canonicalRepresentation,
    supportsMissingValues
) {
    final override fun toString() : String = "Numeric"
    final override fun equals(other : Any?) : Boolean = other is Numeric<*, *>
    final override fun hashCode() : Int = Numeric::class.hashCode()

    /**
     * Placeholder-implementation of [Numeric].
     */
    class PlaceHolder(
        supportsMissingValues: Boolean
    ) : Numeric<PlaceHolder, PlaceHolder.CanonicalRepresentation>(
        CanonicalRepresentation(),
        supportsMissingValues
    ) {
        override fun copy() : PlaceHolder = PlaceHolder(supportsMissingValues)

        class CanonicalRepresentation: NumericCanonicalRepresentation<CanonicalRepresentation, PlaceHolder>() {
            override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PlaceHolder, I>) : Double {
                // This is the only representation for this type, so I always is Double
                return value as Double
            }
        }
    }
}
