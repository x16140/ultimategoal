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

    val initial: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    val m5 by Motor
    val s3 by Servo
    val s1 by Servo

    var current
        get() = if (mode) a else b
        set(v) { if (mode) a = v else b = v }

    var done = false

    var mode = true

    val shooterPositionA = 0.7
    val shooterPositionB = 1.0
    val shootDelay = 50L

    var a = initial.shooterHigh
    var b = initial.shooterPower
    var sdl = initial.shootDelay ?: 250L

    override suspend fun loop() {
        if (done)
            return

        s3.position = current

        gamepad {
            click(Controller::a) {
                PersistentObject.save(CalibrationData(
                    shooterHigh = a,
                    shooterPower = b,
                    shootDelay = sdl
                ), "/sdcard/calibration.dat")

                done = true
                log.add("Done! Please exit the program.").update()
            }

            click(Controller::b) {
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

            click(Controller::x) {
                sdl -= 50L
            }

            click(Controller::y) {
                sdl += 50L
            }

            active {
                m5.power(if (gamepad.rt >= 0.5) -1.0 else 0.0)
            }

            click(Controller::rb) {
                GlobalScope.async {
                    while (+gamepad.rb) {
                        s1.position = shooterPositionA
                        Thread.sleep(shootDelay)

                        s1.position = shooterPositionB
                        Thread.sleep(sdl)
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
            .add("")
            .add("Shoot Delay: ${sdl}ms")
            .update()
    }
}