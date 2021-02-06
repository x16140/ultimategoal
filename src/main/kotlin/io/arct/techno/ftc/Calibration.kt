package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.hardware.input.Gamepad
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.input.DPad
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
        if (done)
            return

        s3.position = current

        gamepad {
            click(Gamepad::a) {
                PersistentObject.save(CalibrationData(
                    shooterHigh = a,
                    shooterPower = b
                ), "/sdcard/calibration.dat")

                done = true
                log.add("Done! Please exit the program.").update()
            }

            click(Gamepad::b) {
                mode = !mode
            }

            click(DPad::up) {
                current += 0.01
            }

            click(DPad::down) {
                current -= 0.01
            }

            click(DPad::left) {
                current -= 0.1
            }

            click(DPad::right) {
                current += 0.1
            }

            active {
                m5.power(if (gamepad.rt >= 0.5) -1.0 else 0.0)
            }

            click(Gamepad::rb) {
                GlobalScope.async {
                    while (+gamepad.rb) {
                        s1.position = shooterPositionA
                        Thread.sleep(shootDelay)

                        s1.position = shooterPositionB
                        Thread.sleep(shootDelay * 3)
                    }
                }
            }
        }

        if (current > 1)
            current = 1.0

        if (current < -1)
            current = -1.0

        log
            .add("Currently Calibrating ${if (mode) "High Goal" else "PowerShot Target"}")
            .add("Value: $current")
            .update()
    }
}