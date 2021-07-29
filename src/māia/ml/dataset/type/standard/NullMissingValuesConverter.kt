package māia.ml.dataset.type.standard

import māia.ml.dataset.type.MissingValuesConverter


/**
 * Converter which uses nullability to represent the missing/not missing status
 * of values.
 */
class NullMissingValuesConverter<T : Any> : MissingValuesConverter<T, T?>() {
    override fun isMissing(value : T?) : Boolean = value == null
    override fun convertNotMissingToBase(value : T?) : T = value!!
    override fun convertBaseToNotMissing(value : T) : T = value
    override fun missing() : T? = null
}
