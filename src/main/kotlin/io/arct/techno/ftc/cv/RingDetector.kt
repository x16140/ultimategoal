package io.arct.techno.ftc.cv

import android.graphics.Bitmap
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import com.vuforia.PIXEL_FORMAT
import com.vuforia.Vuforia
import io.arct.ftc.eventloop.OperationMode
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import kotlin.math.max
import kotlin.math.min

class RingDetector(
    map: HardwareMap,
    key: String,
    cameraDirection: VuforiaLocalizer.CameraDirection = VuforiaLocalizer.CameraDirection.FRONT
) {
    companion object {
        val rings = listOf(
            Rectangle(1291, 1225, 157, 27),
            Rectangle(11256, 1028, 239, 157)
        )

        val orangeH = 15.0..50.0
        val orangeS = 60.0..100.0
        val orangeV = 60.0..100.0
    }

    init {
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true)
    }

    private val vuforia = ClassFactory.getInstance().createVuforia(VuforiaLocalizer.Parameters(
            map.appContext.resources.getIdentifier("cameraMonitorViewId", "id", map.appContext.packageName)
    ).also {
        it.vuforiaLicenseKey = key
        it.cameraDirection = cameraDirection
    }).also {
        it.enableConvertFrameToBitmap()
        it.frameQueueCapacity = 5
    }

    fun scan(): RingState {
        val frame = vuforia.frameQueue.take()
        val bitmap = vuforia.convertFrameToBitmap(frame)
        frame.close()

        val (bottom, top) = rings.map { Bitmap.createBitmap(bitmap, it.x, it.y, it.width, it.height).average.orange }

        return when {
            !bottom -> RingState.None
            !top    -> RingState.Partial
            else    -> RingState.Full
        }
    }
}

val Bitmap.average: Color get() {
    val total = this.width * this.height

    val pixels = IntArray(this.width * this.height)
    this.getPixels(pixels, 0, width, 0, 0, width, height)

    return pixels.map {
        Triple(android.graphics.Color.red(it), android.graphics.Color.blue(it), android.graphics.Color.green(it))
    }.reduce { acc, triple ->
        Triple(acc.first + triple.first, acc.second + triple.second, acc.third + triple.third)
    }.run {
        Color(
            this.first / total,
            this.second / total,
            this.third / total
        )
    }
}

val Color.hsv: Triple<Double, Double, Double> get() {
    val r = this.r.toDouble() / 255
    val g = this.g.toDouble() / 255
    val b = this.b.toDouble() / 255

    val cmax = max(r, max(g, b))
    val cmin = min(r, min(g, b))
    val diff = cmax - cmin

    val h = when (cmax) {
        cmin -> .0
        r    -> (60 * ((g - b) / diff) + 360) % 360
        g    -> (60 * ((b - r) / diff) + 120) % 360
        b    -> (60 * ((r - g) / diff) + 240) % 360
        else -> -1.0
    }

    val s = if (cmax == .0) .0 else (diff / cmax) * 100
    val v = cmax * 100

    return Triple(h, s, v)
}

val Color.orange: Boolean get() {
    val (h, s, v) = hsv
    return RingDetector.orangeH.contains(h) && RingDetector.orangeS.contains(s) && RingDetector.orangeV.contains(v)
}