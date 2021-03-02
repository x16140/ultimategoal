package io.arct.techno.ftc.cv

import android.graphics.Bitmap
import android.util.Log
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import com.vuforia.PIXEL_FORMAT
import com.vuforia.Vuforia
import io.arct.ftc.eventloop.OperationMode
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

class RingDetector(
    private val program: OperationMode,
    key: String,
    cameraDirection: VuforiaLocalizer.CameraDirection = VuforiaLocalizer.CameraDirection.FRONT,
) {
    companion object {
        val rings = listOf(
            Rectangle(1066, 233, 59, 17),
            Rectangle(1060, 247, 96, 78)
        )

        val orangeH = 15.0..50.0
        val orangeS = 30.0..100.0
        val orangeV = 30.0..100.0

        val tolerance = .5
    }

    init {
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true)
    }

    private val vuforia = ClassFactory.getInstance().createVuforia(VuforiaLocalizer.Parameters(
            program.__get_sdk().hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", program.__get_sdk().hardwareMap.appContext.packageName)
    ).also {
        it.vuforiaLicenseKey = key
        it.cameraDirection = cameraDirection
    }).also {
        it.enableConvertFrameToBitmap()
        it.frameQueueCapacity = 5
    }

    fun scan(): RingState {
        val frame = vuforia.frameQueue.take()
        val bitmap = vuforia.convertFrameToBitmap(frame)!!
        frame.close()

//        save(bitmap, "full")

        val (bottom, top) = rings.mapIndexed { i, it ->
            Bitmap.createBitmap(bitmap, it.x, it.y, it.width, it.height).orange >= (1 - tolerance)
        }

        return when {
            !bottom -> RingState.None
            !top    -> RingState.Partial
            else    -> RingState.Full
        }
    }

    fun save(bitmap: Bitmap, label: Any) {
        try {
            val f = File("/sdcard/$label.jpg")

            if (f.exists())
                f.delete()

            val stream = FileOutputStream(f)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

val Bitmap.orange: Double get() {
    val total = this.width * this.height
    val pixels = IntArray(total)
    this.getPixels(pixels, 0, width, 0, 0, width, height)

    return pixels.map {
        Triple(
            android.graphics.Color.red(it),
            android.graphics.Color.green(it),
            android.graphics.Color.blue(it)
        ).hsv.orange
    }.count { it }.toDouble() / total
}

val Triple<Int, Int, Int>.hsv: Color get() {
    val r = this.first.toDouble() / 255
    val g = this.second.toDouble() / 255
    val b = this.third.toDouble() / 255

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

    return Color(h, s, v)
}

val Color.orange: Boolean get() {
    return RingDetector.orangeH.contains(h) && RingDetector.orangeS.contains(s) && RingDetector.orangeV.contains(v)
}