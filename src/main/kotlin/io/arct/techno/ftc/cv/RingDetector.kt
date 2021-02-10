package io.arct.techno.ftc.cv

import android.graphics.Bitmap
import com.vuforia.PIXEL_FORMAT
import com.vuforia.Vuforia
import io.arct.ftc.eventloop.OperationMode
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer

class RingDetector(
    program: OperationMode,
    key: String,
    cameraDirection: VuforiaLocalizer.CameraDirection = VuforiaLocalizer.CameraDirection.FRONT
) {
    companion object {
        val rings = listOf(
            Rectangle(1291, 1225, 157, 27),
            Rectangle(11256, 1028, 239, 157)
        )
    }

    init {
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true)
    }

    private val vuforia = ClassFactory.getInstance().createVuforia(VuforiaLocalizer.Parameters().also {
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

val Color.orange: Boolean get() {
    return false
}