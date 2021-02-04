package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.techno.ftc.util.CalibrationData
import io.arct.techno.ftc.util.PersistentObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@OperationMode.Register(OperationMode.Type.Operated, "Calibration")
class Calibration : OperationMode() {
    val gamepad = Controller.get("gamepad0")

    val m5 by Motor
    val s3 by Servo
    val s1 by Servo

    var current
        get() = if (mode) a else b
        set(v) { if (mode) a = v else b = v }

    var done = false

    var mode = false

    val shooterPositionA = 0.7
    val shooterPositionB = 1.0
    val shootDelay = 50L

    var a = 0.0
    var b = 0.0

    var bb = false
    var u = false
    var d = false
    var l = false
    var r = false
    var rb = false

    override suspend fun loop() {
        if (done) {
            return
        }

        if (gamepad.a) {
            PersistentObject.save(CalibrationData(
                shooterHigh = a,
                shooterPower = b
            ), "/sdcard/calibration.dat")

            done = true
            log.add("Done! Please exit the program.").update()
        }

        if (gamepad.b) {
            if (!bb) mode = !mode
            bb = true
        } else bb = false

        if (gamepad.dpad.up) {
            if (!u) current += 0.01
            u = true
        } else u = false

        if (gamepad.dpad.down) {
            if (!d) current -= 0.01
            d = true
        } else d = false

        if (gamepad.dpad.left) {
            if (!l) current -= 0.1
            l = true
        } else l = false

        if (gamepad.dpad.right) {
            if (!r) current += 0.1
            r = true
        } else r = false

        if (current > 1)
            current = 1.0

        if (current < -1)
            current = -1.0

        m5.power(if (gamepad.rt >= 0.5) -1.0 else 0.0)
        s3.position = current

        if (gamepad.rb) {
            if (!rb) {
                rb = true

                GlobalScope.async {
                    s1.position = shooterPositionA
                    Thread.sleep(shootDelay)
                    s1.position = shooterPositionB
                    Thread.sleep(shootDelay * 3)
                    rb = false
                }
            }
        } else rb = false

        log
            .add("Currently Calibrating ${if (mode) "High Goal" else "PowerShot Target"}")
            .add("Value: $current")
            .update()
    }
}