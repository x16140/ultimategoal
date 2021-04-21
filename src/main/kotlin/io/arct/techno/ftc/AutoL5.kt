package io.arct.techno.ftc

/*

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.units.*
import io.arct.techno.ftc.cv.RingDetector
import io.arct.techno.ftc.cv.RingState
import io.arct.techno.ftc.util.*
import kotlin.math.abs
import kotlin.math.sign

@OperationMode.Register(OperationMode.Type.Autonomous, "Auto (L5)")
class AutoL5 : LinearOperationMode() {
    private val shooter = Motor.get("m5", 5800.revpm)
    private val wobble = Motor.get("m7", 223.revpm)

    private val gamepad0 by Controller

    private val s1 by Servo
    private val s2 by Servo
    private val s3 by Servo

    private val secrets: Secret = PersistentObject.load("/sdcard/secrets.dat")
    private val calibration: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    private val gyro = imu
    private val rotation get() = gyro.orientation.x

    private val robot: Robot = robot()
    private val detector = RingDetector(this, secrets.vuforia)

    val q = 1.7.mps
    val k = 1.5.mps
    val f = 1.3.mps
    val s = 1.mps

    val shooterPower = -1.0
    val shootDelay = 400L
    val shooterPositionA = 0.7
    val shooterPositionB = 1.0

    var mode = 0

    override suspend fun init() {
        s2.position = -0.8
        s3.position = calibration.shooterHigh

        var done = false

        while (!done) gamepad0 {
            click(Controller::x) {
                mode = 1
            }

            click(Controller::y) {
                mode = 2
            }

            click(Controller::b) {
                mode = 3
            }

            click(Controller::a) {
                done = true
            }

            active {
                log.add("""
(∅) Ring Detection ${if (mode == 0) "<" else ""}
(x) No Rings ${if (mode == 1) "<" else ""}
(y) Partial Rings ${if (mode == 2) "<" else ""}
(b) Full Rings ${if (mode == 3) "<" else ""}
""").update()
            }
        }
    }

    override suspend fun run() {
        val state = when (mode) {
            0 -> detector.scan()
            1 -> RingState.None
            2 -> RingState.Partial
            3 -> RingState.Full
            else -> TODO()
        }

        robot.move(Angle.Forward, 150.cm, f)

        revUp()

        robot.move(Angle.Left, 40.cm, s)

        correct()
        shoot(3)

        when (state) {
            RingState.None -> none()
            RingState.Partial -> partial()
            RingState.Full -> full()
        }
    }

    private suspend fun none() {
        robot.move(Angle.Forward, 55.cm, s)
        robot.move(Angle.Left, 22.cm, s)

        dropWobble(stored = true, up = true)

        robot.move(Angle.Right, (56 - 40 - 5).cm, s)
        correct()
        robot.move(Angle.Backward, 147.cm, k)

        correct()
        pickupWobble(down = true, store = false)

        robot.move(Angle.Forward, 147.cm, f)
        robot.move(Angle.Left, (56 - 38 + 15 - 5).cm, s)

        correct()
        dropWobble(stored = false, up = true)

        robot.move(Angle.Right, 20.cm, s)
        robot.move(Angle.Backward, 20.cm, f)
    }

    private suspend fun partial() {
        robot.move(Angle.Forward, 119.cm, f)
        robot.move(Angle.Right, 30.cm, s)

        correct()
        dropWobble(stored = true, up = true)

        correct()
        robot.move(Angle.Backward, (220 - 9 + 5).cm, k)

        liftDownWobble()                            // ----+
                                                    //     |
        robot.move(Angle.Left, (23 + 15 + 8 + 4).cm, s) // |
        correct()                                   //     |
                                                    //     |
        wobble.stop()                               // ----+

        pickupWobble(down = false, store = false)

        robot.move(Angle.Right, (38 + 4).cm, s)
        correct()
        robot.move(Angle.Forward, (200 - 9 + 5).cm, q)

        correct()
        dropWobble(stored = false, up = true)

        robot.move(Angle.Backward, 72.cm, f)
//        robot.move(Angle.Left, 5.cm, s)
    }

    private suspend fun full() {
        robot.move(Angle.Left, 35.cm, s)

        robot.move(Angle.Forward, 150.cm, q)
        robot.move(Angle.Forward, s); Thread.sleep(500L); robot.stop()

        dropWobble(stored = true, up = true)

        robot.move(Angle.Backward, 10.cm, q)
        correct()
        robot.move(Angle.Right, 10.cm, s)

        liftDownWobble()                            // ---+
        robot.move(Angle.Backward, (242 - 5 + 5).cm, q) //    |
        wobble.stop()                               // ---+

        correct()
        pickupWobble(down = true, store = false)
        robot.move(Angle.Left, 5.cm, s)
        correct()

        robot.move(Angle.Forward, (250 - 5 + 5).cm, q)
        robot.move(Angle.Forward, s); Thread.sleep(500L); robot.stop()

        correct()
        dropWobble(stored = false, up = true)

        robot.move(Angle.Right, 5.cm, s)
        robot.move(Angle.Backward, 122.cm, q)
    }

    private fun dropWobble(stored: Boolean, up: Boolean) {
        if (stored) {
            wobble.power(-.8)
            Thread.sleep(800L)
            wobble.stop()
        }

        Thread.sleep(200L)
        s2.position = 0.8
        Thread.sleep(600L)

        if (up) {
            wobble.power(.6)
            Thread.sleep(500L)
            wobble.stop()
        }
    }

    private fun pickupWobble(down: Boolean, store: Boolean) {
        if (down) {
            wobble.power(-.8)
            Thread.sleep(800L)
            wobble.stop()
        }

        Thread.sleep(200L)
        s2.position = -0.8
        Thread.sleep(800L)

        if (store) {
            wobble.power(.6)
            Thread.sleep(500L)
            wobble.stop()
        }
    }

    private fun liftUpWobble() {
        wobble.power(.6)
        Thread.sleep(500L)
        wobble.stop()
    }

    private fun liftDownWobble() {
        wobble.power(-.8)
    }

    private fun revUp() {
        shooter.power(shooterPower)
    }

    private fun shoot(n: Int) {
        for (i in 1..n) {
            Thread.sleep(shootDelay)
            s1.position = shooterPositionA
            Thread.sleep(shootDelay)
            s1.position = shooterPositionB
        }

        shooter.stop()
    }

    private fun correct() {
        while (abs(rotation.deg.value) > 2)
            robot.rotate(.3.mps * sign(rotation.deg.value))

        robot.stop()
    }
}

*/
