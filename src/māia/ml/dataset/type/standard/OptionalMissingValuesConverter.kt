package māia.ml.dataset.type.standard

import māia.ml.dataset.type.MissingValuesConverter
import māia.util.Absent
import māia.util.Optional
import māia.util.Present

/**
 * Converter which uses [Optional] to represent the missing/not missing status
 * of values.
 */
class OptionalMissingValuesConverter<T> : MissingValuesConverter<T, Optional<T>>() {
    override fun isMissing(value : Optional<T>) : Boolean = value is Absent
    override fun convertNotMissingToBase(value : Optional<T>) : T = value.get()
    override fun convertBaseToNotMissing(value : T) : Optional<T> = Present(value)
    override fun missing() : Optional<T> = Absent
}
