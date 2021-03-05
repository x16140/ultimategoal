package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.eventloop.OperationMode.Type
import io.arct.ftc.hardware.input.Gamepad
import io.arct.rl.control.ArcadeControl
import io.arct.rl.control.MecanumControl
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.robot.drive.MecanumDrive
import io.arct.rl.units.*
import io.arct.techno.ftc.jank.JankDrive
import io.arct.techno.ftc.jank.LessJankDrive
import io.arct.techno.ftc.util.CalibrationData
import io.arct.techno.ftc.util.PersistentObject
import io.arct.techno.ftc.util.mecanum
import io.arct.techno.ftc.util.robot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@OperationMode.Register(Type.Operated, name = "TeleOp")
class Operated : OperationMode() {
    private val m5 = Motor.get("m5", 5800.revpm)
    private val m6 = Motor.get("m6", 312.revpm)
    private val m7 = Motor.get("m7", 235.revpm)
    private val m8 = Motor.get("m8", 312.revpm)

    private val s1 by Servo
    private val s2 by Servo
    private val s3 by Servo
    private val s4 by Servo

    private val gamepad0 by Controller
    private val gamepad1 by Controller

    val robot: Robot = robot(odometry = false)

    val spotTurnVelocity = -1.0
    val precisionSpotTurnVelocity = -0.3
    val wobbleSpeed = 0.5
    val precisionWobbleSpeed = 0.3
    val gripperPositionA = -0.8
    val gripperPositionB = 0.8
    val shooterPositionA = 0.7
    val shooterPositionB = 1.0
    val intakePowerA = 1.0
    val intakePowerB = 0.70
    val shooterPower = -1.0
    val shootDelay = 50L
    val stickPositionA = .05
    val stickPositionB = .4

    val calibration: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    val mecanum = MecanumControl(robot, Controller::left, invertX = true, invertY = true)
    val arcade = ArcadeControl(robot, Controller::right, invertY = true)

    val jank = LessJankDrive(robot)

    override suspend fun loop() {
        gamepad0 {
//            active {
//                val turn = gamepad0.lt != .0 || gamepad0.rt != .0 || +gamepad0.lb || +gamepad0.rb
//
//                if (!turn)
//                    mecanum.apply(gamepad0)
//
//                if (gamepad0.left.origin && !turn)
//                    arcade.apply(gamepad0)
//            }

            active {
                jank.apply(gamepad0)
            }

            active(Controller::lt, .0) {
                robot.rotate(robot.velocity * (gamepad0.lt * spotTurnVelocity))
            }

            active(Controller::rt, .0) {
                robot.rotate(robot.velocity * (gamepad0.rt * -spotTurnVelocity))
            }

            active(Controller::lb) {
                robot.rotate(robot.velocity * precisionSpotTurnVelocity)
            }

            active(Controller::rb) {
                robot.rotate(robot.velocity * -precisionSpotTurnVelocity)
            }
        }

        gamepad1 {
            active {
                m7.power(gamepad1.left.y * wobbleSpeed + gamepad1.right.y * precisionWobbleSpeed)
            }

            active {
                s2.position = if (+gamepad1.y) gripperPositionA else gripperPositionB
                s3.position = if (+gamepad1.x) calibration.shooterPower else calibration.shooterHigh
            }

            active {
                when {
                    gamepad1.lt >= 0.5 -> {
                        m6.power(intakePowerA * 0.6)
                        m8.power(intakePowerB * 0.6)
                    }

                    +gamepad1.lb -> {
                        m6.power(-intakePowerA)
                        m8.power(-intakePowerB)
                    }

                    else -> {
                        m6.power(0.0)
                        m8.power(0.0)
                    }
                }
            }

            active {
                s4.position = if (+gamepad1.b) stickPositionA else stickPositionB
            }

            active {
                if (gamepad1.rt >= 0.5)
                    m5.power(shooterPower)
                else
                    m5.power(0.0)
            }

            click(Controller::rb) {
                GlobalScope.async {
                    while (+gamepad1.rb) {
                        s1.position = shooterPositionA
                        Thread.sleep(shootDelay)

                        s1.position = shooterPositionB
                        Thread.sleep(shootDelay * 3)
                    }
                }
            }
        }
    }
}