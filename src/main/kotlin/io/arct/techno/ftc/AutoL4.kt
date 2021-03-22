package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.robot.position.DynamicPositioning
import io.arct.rl.robot.position.TripleOdometry
import io.arct.rl.units.*
import io.arct.techno.ftc.cv.RingDetector
import io.arct.techno.ftc.cv.RingState
import io.arct.techno.ftc.util.*
import kotlin.math.abs

@OperationMode.Register(OperationMode.Type.Autonomous, "Auto (L4)")
class AutoL4 : LinearOperationMode() {
    private val shooter = Motor.get("m5", 5800.revpm)
    private val wobble = Motor.get("m7", 223.revpm)

    private val s1 by Servo
    private val s2 by Servo
    private val s3 by Servo

    private val secrets: Secret = PersistentObject.load("/sdcard/secrets.dat")
    private val calibration: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    private val robot: Robot = robotL4()
    private val detector = RingDetector(this, secrets.vuforia)
//    private val adjust = JankAdjustOdometry(robot, mecanum, odometers)

    val shooterPower = -1.0
    val shootDelay = 500L
    val shooterPositionA = 0.7
    val shooterPositionB = 1.0

    override suspend fun init() {
        s2.position = -0.8
        s3.position = calibration.shooterHigh
    }

    override suspend fun run() {
        val state = detector.scan()

        dynMove(145.cm, 1.mps)
//        robot.move(Angle.Forward, 145.cm, 1.mps)
        robot.move(Angle.Left, 240.cm, 1.mps)

        shoot(3)

        when (state) {
            RingState.None -> {
                robot.move(Angle.Left, 100.cm, 1.mps)
                dynMove(50.cm, 1.mps)
//                robot.move(Angle.Forward, 50.cm, 1.mps)
            }

            RingState.Partial -> {
                robot.move(Angle.Right, (140 + 160 - 60).cm, 1.mps)
                dynMove((60 + 50).cm, 1.mps)
//                robot.move(Angle.Forward, (60 + 50).cm, 1.mps)
            }

            RingState.Full -> {
                robot.move(Angle.Left, 100.cm, 1.mps)
                dynMove((80 + 50).cm, 1.mps)
//                robot.move(Angle.Forward, (80 + 50).cm, 1.mps)

                robot.move(Angle.Forward, 1.mps)
                Thread.sleep(1000L)
                robot.stop()
            }
        }

        wobble.power(-.8)
        Thread.sleep(500L)
        wobble.stop()

        s2.position = 0.8
        Thread.sleep(1500L)

        wobble.power(.6)
        Thread.sleep(500L)
        wobble.stop()

        when (state) {
            RingState.None -> {
                robot.move(Angle.Right, 100.cm, 1.mps)
                robot.move(Angle.Backward, 25.cm, 1.mps)
            }

            RingState.Partial -> {
                robot.move(Angle.Backward, (60 + 25).cm, 1.mps)
                robot.move(Angle.Left, (140 + 160 - 60).cm, 1.mps)
            }

            RingState.Full -> {
//                robot.move(Angle.Right, 100.cm, 1.mps)
                robot.move(Angle.Backward, (100 + 25).cm, 1.mps)
            }
        }
    }

    private fun shoot(n: Int) {
        shooter.power(shooterPower)
        Thread.sleep(1000L)

        for (i in 0..n) {
            Thread.sleep(shootDelay * 2)
            s1.position = shooterPositionA
            Thread.sleep(shootDelay)
            s1.position = shooterPositionB
        }

        shooter.stop()
    }

    suspend fun dynMove(distance: Distance, spd: Velocity) {
        val odometry: TripleOdometry = robot.positioning as TripleOdometry
        odometry.zero()

        val initial = robot.position
        val fwd = spd > 0.mps
        val direction = if (fwd) Angle.Forward else Angle.Backward
        val speed = abs(spd.mps.value).mps
        robot.move(direction, speed)

        while (initial distance robot.position <= distance && active) {
            DynamicPositioning.updateLinear(robot.positioning)

            val y1 = odometry.y1.position
            val y2 = odometry.y2.position

            if (fwd) when {
                abs((y2 - y1).cm.value) < 5 -> robot.move(direction, speed)
                y1 > y2 -> robot.turn(speed * 0.99, speed)
                y1 < y2 -> robot.turn(speed * 0.99, -speed)
            } else when {
                abs((y2 - y1).cm.value) < 5 -> robot.move(direction, speed)
                y1 < y2 -> robot.turn(speed * 0.99, speed)
                y1 > y2 -> robot.turn(speed * 0.99, -speed)
            }
        }

        stop()
    }
}