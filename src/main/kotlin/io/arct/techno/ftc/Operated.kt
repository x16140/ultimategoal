package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.eventloop.OperationMode.Type
import io.arct.rl.control.ArcadeControl
import io.arct.rl.control.MecanumControl
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.robot.drive.MecanumDrive
import io.arct.rl.robot.position.NoPositioning
import io.arct.rl.robot.robot
import io.arct.rl.units.*
import io.arct.techno.ftc.util.CalibrationData
import io.arct.techno.ftc.util.PersistentObject
import io.arct.techno.ftc.util.mecanum
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.coroutines.coroutineContext

@OperationMode.Register(Type.Operated, name = "TeleOp")
class Operated : OperationMode() {
    private val m5 = Motor.get("m5", 5800.revpm)
    private val m6 = Motor.get("m6", 312.revpm)
    private val m7 = Motor.get("m7", 235.revpm)
    private val m8 = Motor.get("m8", 312.revpm)

    private val s1 by Servo
    private val s2 by Servo
    private val s3 by Servo

    private val gamepad0 by Controller
    private val gamepad1 by Controller

    val drive: MecanumDrive = mecanum(this)

    val robot: Robot = robot {
        using drive       drive
        using positioning NoPositioning(true)
    }

    val spotTurnVelocity = -1.0
    val precisionSpotTurnVelocity = -0.3
    val wobbleSpeed = 0.5
    val precisionWobbleSpeed = 0.3
    val shooterHeightA = 0.95
    val shooterHeightB = 1.0
    val gripperPositionA = -0.8
    val gripperPositionB = 0.8
    val gripperPositionC = -0.6
    val shooterPositionA = 0.7
    val shooterPositionB = 1.0
    val intakePowerA = 1.0
    val intakePowerB = 0.85
    val shooterPower = -1.0
    val shootDelay = 50L

    val calibration: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    val mecanum = MecanumControl(drive, Controller::left)
    val arcade = ArcadeControl(drive, Controller::right)
//    val pmecanum = MecanumControl(drive, Controller::left, maxSpeed = .3)
//    val parcade = ArcadeControl(drive, Controller::right, maxSpeed = .3)
    val imecanum = MecanumControl(drive, Controller::left, invertX = true, invertY = true)
    val iarcade = ArcadeControl(drive, Controller::right, invertY = true)
//    val ipmecanum = MecanumControl(drive, Controller::left, maxSpeed = .3, invertX = true, invertY = true)
//    val iparcade = ArcadeControl(drive, Controller::right, maxSpeed = .3, invertY = true)

    var precisionDrive = false
    var gripper = false
    var variableShooter = false
    var invertDrive = !false

    var p0a = false
    var p0b = false
    var p1a = false
    var p1y = false
    var p1rb = false
    var p1b = false

    override suspend fun loop() {
        // Gamepad 1
//        if (precisionDrive) {
////            val useMecanum = if (invertDrive) ipmecanum else pmecanum
////            val useArcade = if (invertDrive) iparcade else parcade
//
//            useMecanum.apply(gamepad0)
//
//            if (gamepad0.left.origin)
//                useArcade.apply(gamepad0)
//        } else {
//            val useMecanum = if (invertDrive) imecanum else mecanum
//            val useArcade = if (invertDrive) iarcade else arcade
//
//            useMecanum.apply(gamepad0)
//
//            if (gamepad0.left.origin)
//                useArcade.apply(gamepad0)
//        }

        val turn = gamepad0.lt != .0 || gamepad0.rt != .0 || gamepad0.lb || gamepad0.rb

        if (!turn)
            imecanum.apply(gamepad0)

        if (gamepad0.left.origin && !turn)
            iarcade.apply(gamepad0)

        if (gamepad0.a) if (!p0a) {
            precisionDrive = !precisionDrive
            p0a = true
        } else
            p0a = false

//        if (gamepad0.b) if (!p0b) {
//            invertDrive = !invertDrive
//            p0b = true
//        } else
//            p0b = false

        if (gamepad0.lt != .0 || gamepad0.rt != .0)
            robot.rotate(robot.velocity * (gamepad0.lt * spotTurnVelocity + gamepad0.rt * -spotTurnVelocity))

        if (gamepad0.lb)
            robot.rotate(robot.velocity * precisionSpotTurnVelocity)

        if (gamepad0.rb)
            robot.rotate(robot.velocity * -precisionSpotTurnVelocity)

        // Gamepad 2
        m7.power(gamepad1.left.y * wobbleSpeed + gamepad1.right.y * precisionWobbleSpeed)

//        if (gamepad1.a) if (!p1a) {
//            gripper = !gripper
//            p1a = true
//        } else
//            p1a = false

//        if (gamepad1.y) if (!p1y) {
//            variableShooter = !variableShooter
//            p1y = true
//        } else
//            p1y = false

//        s2.position = when (gripper) {
//            0 -> gripperPositionA
//            1 -> gripperPositionB
//            2 -> gripperPositionC
//
//            else -> gripperPositionA
//        }

        s2.position = if (gamepad1.y) gripperPositionA else gripperPositionB
        s3.position = if (gamepad1.x) calibration.shooterPower else calibration.shooterHigh

        if (gamepad1.lt >= 0.5) {
            m6.power(intakePowerA * 0.6)
            m8.power(intakePowerB * 0.6)
        } else if (gamepad1.lb) {
            m6.power(-intakePowerA)
            m8.power(-intakePowerB)
        } else {
            m6.power(0.0)
            m8.power(0.0)
        }

        if (gamepad1.rt >= 0.5)
            m5.power(shooterPower)
        else
            m5.power(0.0)

        if (gamepad1.rb) {
            if (!p1rb) {
                p1rb = true

                GlobalScope.async {
                    s1.position = shooterPositionA
                    Thread.sleep(shootDelay)
                    s1.position = shooterPositionB
                    Thread.sleep(shootDelay * 3)
                    p1rb = false
                }
            }
        } else
            p1rb = false
    }
}