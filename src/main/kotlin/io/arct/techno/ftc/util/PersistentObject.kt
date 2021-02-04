package io.arct.techno.ftc.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class CalibrationData(
    val shooterHigh: Double,
    val shooterPower: Double
)

object PersistentObject {
    inline fun<reified T> save(value: T, path: String): Unit =
        File(path).writeText(Json.encodeToString(value), charset("utf-8"))

    inline fun<reified T> load(path: String): T =
        Json.decodeFromString(File(path).readText(charset("utf-8")))
}