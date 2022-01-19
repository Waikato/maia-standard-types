package maia.ml.dataset.type.standard

import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.DataType

/**
 * Base-class for implementations of the canonical representation of
 * the [UntypedData] data-type. This representation presents values as Kotlin's
 * base type, [Any]?.
 *
 * @param Self See [DataRepresentation].
 * @param D The type of [UntypedData] that owns this representation.
 */
abstract class UntypedRepresentation<
        Self: UntypedRepresentation<Self, D>,
        D: UntypedData<D, Self>
> : DataRepresentation<Self, D, Any?>()
{
    final override fun isValid(value : Any?) : Boolean = true
    final override fun initial() : Any? = null
}

/**
 * Base-class for implementations of untyped data-types, where
 * values can be anything.
 *
 * @param canonicalRepresentation See [DataType].
 * @param supportsMissingValues See [DataType].
 *
 * @param Self See [DataType].
 * @param C See [DataType].
 */
abstract class UntypedData<
        Self: UntypedData<Self, C>,
        C: UntypedRepresentation<C, Self>
>(
    canonicalRepresentation : C,
    supportsMissingValues: Boolean
): DataType<Self, C>(
    canonicalRepresentation,
    supportsMissingValues
) {
    final override fun toString() : String = "Untyped"
    final override fun equals(other : Any?) : Boolean = other is UntypedData<*, *>
    final override fun hashCode() : Int = UntypedData::class.hashCode()

    /**
     * Place-holder implementation of [UntypedData].
     */
    class PlaceHolder(
        supportsMissingValues: Boolean
    ): UntypedData<PlaceHolder, PlaceHolder.Representation>(
        Representation(),
        supportsMissingValues
    ) {
        override fun copy() : PlaceHolder = PlaceHolder(supportsMissingValues)

        class Representation: UntypedRepresentation<Representation, PlaceHolder>() {
            override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PlaceHolder, I>) : Any? {
                return value
            }
        }
    }
}
