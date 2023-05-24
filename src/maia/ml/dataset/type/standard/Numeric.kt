package maia.ml.dataset.type.standard

import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.DataType
import maia.ml.dataset.type.EntropicRepresentation
import maia.ml.dataset.type.FiniteDataType
import maia.util.error.UNREACHABLE_CODE
import maia.util.minus
import maia.util.plus
import maia.util.times
import java.math.BigInteger

/** The number of possible non-NaN double values. */
val DOUBLE_ENTROPY: BigInteger = (
            (
                (BigInteger.ONE.shiftLeft(11) - 1) // Number of normal/sub-normal exponents (excl. NaN/Inf exponent 0x3FF)
                * BigInteger.ONE.shiftLeft(52) // Number of mantissa values
                + 1 // Infinity
            ) * 2 // Positive and negative values, including negative zero
        )

/**
 * Canonical [representation][DataRepresentation] of the [Numeric]
 * [data-type][DataType]. This representation presents values as [Double]
 * numeric values.
 */
class NumericCanonicalRepresentation:
    DataRepresentation<NumericCanonicalRepresentation, Numeric, Double>()
{
    override fun isValid(value : Double) : Boolean = !value.isNaN()
    override fun initial() : Double = 0.0
    override fun <I> convertValue(
        value : I,
        fromRepresentation: DataRepresentation<*, Numeric, I>
    ): Double {
        return when (fromRepresentation) {
            is NumericCanonicalRepresentation -> value as Double
            is NumericEntropicRepresentation -> {
                // Get the binary representation
                val bits = (value as BigInteger).toLong()

                // Shift the sign bit to the end
                (bits ushr 1) or (bits shl 63)

                return Double.fromBits(bits)

            }
            else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
        }
    }
}
/**
 * [Entropic representation][EntropicRepresentation] of the [Numeric]
 * [data-type][DataType].
 */
class NumericEntropicRepresentation: EntropicRepresentation<NumericEntropicRepresentation, Numeric>()
{
    override fun <I> convertValue(
        value : I,
        fromRepresentation: DataRepresentation<*, Numeric, I>
    ): BigInteger {
        return when (fromRepresentation) {
            is NumericCanonicalRepresentation -> {
                // Get the binary representation
                val bits = (value as Double).toRawBits()

                // Shift the sign bit to the beginning
                (bits shl 1) or (bits ushr 63)

                return bits.toBigInteger()
            }
            is NumericEntropicRepresentation -> value as BigInteger
            else -> UNREACHABLE_CODE("convertValue is only ever given representations that its data-type declares")
        }
    }
}

/**
 * Numeric data-type, where values are a number.
 *
 * @param supportsMissingValues
 *          See [DataType.supportsMissingValues].
 */
class Numeric(
    supportsMissingValues: Boolean
):
    FiniteDataType<Numeric, NumericCanonicalRepresentation, NumericEntropicRepresentation>(
        NumericCanonicalRepresentation(),
        NumericEntropicRepresentation(),
        supportsMissingValues,
        DOUBLE_ENTROPY
    )
{
    override fun toString() : String = "Numeric"
    override fun equals(other : Any?) : Boolean = other is Numeric
    override fun hashCode() : Int = Numeric::class.hashCode()
    override fun copy() : Numeric = Numeric(supportsMissingValues)
}
