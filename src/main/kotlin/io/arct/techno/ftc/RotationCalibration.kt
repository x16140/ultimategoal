package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.hardware.input.Gamepad
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.input.DPad
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.units.mps
import io.arct.rl.units.rev
import io.arct.techno.ftc.util.CalibrationData
import io.arct.techno.ftc.util.PersistentObject
import io.arct.techno.ftc.util.robot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@OperationMode.Register(OperationMode.Type.Operated, "Rotation Calibration")
class RotationCalibration : OperationMode() {
    val gamepad = Controller.get("gamepad0")

    val robot = robot(this)
    var value = 3.6923

    override suspend fun loop() {
        log.add("" + value).update()

        gamepad {
            click(Controller::a) {
                GlobalScope.async {
                    robot.rotate(4.rev * value, 1.mps)
                }
            }

            click(Controller::b) {
                value += 0
            }

            click(Controller::x) {
                value -= 1
            }

            click(DPad::up) {
                value += 0.1
            }

            click(DPad::down) {
                value -= 0.1
            }

            click(DPad::left) {
                value -= 0.01
            }

            click(DPad::right) {
                value += 0.01
            }
        }
    }
}