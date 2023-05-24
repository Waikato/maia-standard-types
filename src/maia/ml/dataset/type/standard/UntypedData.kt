package maia.ml.dataset.type.standard

import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.DataType
import maia.ml.dataset.type.EntropicRepresentation
import maia.util.error.UNREACHABLE_CODE
import java.math.BigInteger

/**
 * Canonical [representation][DataRepresentation] of the [UntypedData]
 * [data-type][DataType]. This representation presents values as Kotlin's
 * base type, [Any]?.
 */
class UntypedCanonicalRepresentation:
    DataRepresentation<UntypedCanonicalRepresentation, UntypedData, Any?>()
{
    override fun isValid(value : Any?) : Boolean = true
    override fun initial() : Any? = null
    override fun <I> convertValue(
        value : I,
        fromRepresentation: DataRepresentation<*, UntypedData, I>
    ): Any? {
        return value
    }
}

/**
 * Untyped [data-type][DataType], where values can be anything.
 *
 * @param supportsMissingValues
 *          See [DataType.supportsMissingValues].
 */
class UntypedData(
    supportsMissingValues: Boolean
): DataType<UntypedData, UntypedCanonicalRepresentation>(
    UntypedCanonicalRepresentation(),
    supportsMissingValues
) {
    override fun toString() : String = "Untyped"
    override fun equals(other : Any?) : Boolean = other is UntypedData
    override fun hashCode() : Int = UntypedData::class.hashCode()
    override fun copy() : UntypedData = UntypedData(supportsMissingValues)
}
