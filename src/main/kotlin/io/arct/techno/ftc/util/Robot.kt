package io.arct.techno.ftc.util

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.hardware.CRServo
import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.hardware.motors.FBasicMotor
import io.arct.ftc.hardware.sensors.FImu
import io.arct.rl.hardware.motors.ContinuousServo
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.hardware.sensors.DistanceEncoder
import io.arct.rl.robot.Robot
import io.arct.rl.robot.drive.Drive
import io.arct.rl.robot.drive.IDrive
import io.arct.rl.robot.position.DynamicPositioning
import io.arct.rl.robot.position.IPositioning
import io.arct.rl.robot.position.NoPositioning
import io.arct.rl.robot.position.TripleOdometry
import io.arct.rl.robot.robot
import io.arct.rl.units.*
import io.arct.techno.ftc.jank.MecanumCopyJank
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

object Config {
    val yEncoderTicks   = 12.7076
    val xEncoderTicks   = 8.6404

    val turnSpeed       = -1.0
    val precTurnSpeed   = -.3

    val wobbleSpeed     = .5
    val precWobbleSpeed = .3
    val wobbleSpeedDown = -.8
    val wobbleSpeedUp   = .6

    val gripperOpen     = .8
    val gripperClosed   = .4

    val shooterForward  = .7
    val shooterBack     = 1.0

    val intakeBottom    = -1.0
    val intakeTop       = -.75 //0.8

    val shooter         = -1.0
    val shootDelay      = 50L
    val shootDelaySlow  = 750L

    val stickDown       = -.05
    val stickUp         = .5

    val minSpeedStart = .5
    val minSpeedEnd = .25
    val accel = 15.cm
    val decel = 30.cm
}

class Bot(
    val op: OperationMode,
    val hardware: Hardware,
    val robot: Robot
) : IDrive by robot, IPositioning by robot {
    val calibration: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    val shooter = object : Shooter {
        override fun revup() {
            hardware.flywheel.power(Config.shooter)
        }

        override fun revdown() {
            hardware.flywheel.stop()
        }

        override fun shoot() {
            hardware.shoot.position = Config.shooterForward
            Thread.sleep(Config.shootDelay)
            hardware.shoot.position = Config.shooterBack
        }

        override fun shoot(n: Int) {
            repeat(4) {
                shoot()
                Thread.sleep(Config.shootDelaySlow)
            }
        }

        override fun cshoot() {
            shoot()
            Thread.sleep(calibration.shootDelay)
        }

        override fun high() {
            hardware.variableShooter.position = calibration.shooterHigh
        }

        override fun power() {
            hardware.variableShooter.position = calibration.shooterPower
        }
    }

    val wobble = object : Wobble {
        override fun down() {
            downAsync()
            Thread.sleep(800L)
            hardware.wobble.stop()
        }

        override fun up() {
            upAsync()
            Thread.sleep(600L)
            hardware.wobble.stop()
        }

        override fun downAsync() {
            hardware.wobble.power(Config.wobbleSpeedDown)
        }

        override fun upAsync() {
            hardware.wobble.power(Config.wobbleSpeedUp)
        }

        override fun release() {
            Thread.sleep(200L)
            releaseAsync()
            Thread.sleep(600L)
        }

        override fun lock() {
            Thread.sleep(200L)
            lockAsync()
            Thread.sleep(800L)
        }

        override fun releaseAsync() {
            hardware.gripper.position = Config.gripperOpen
        }

        override fun lockAsync() {
            hardware.gripper.position = Config.gripperClosed
        }
    }

    val stick = object : Stick {
        override fun up() {
            hardware.stick.position = Config.stickUp
        }

        override fun down() {
            hardware.stick.position = Config.stickDown
        }
    }

    val intake = object : Intake {
        override fun intake() =
            intake(1.0)

        override fun intake(multiplier: Double) {
            hardware.intakeBottom.power(Config.intakeBottom * multiplier)
            hardware.intakeTop.power(Config.intakeTop * multiplier)
        }

        override fun outtake() =
            outtake(1.0)

        override fun outtake(multiplier: Double) {
            hardware.intakeBottom.power(-Config.intakeBottom * multiplier)
            hardware.intakeTop.power(-Config.intakeTop * multiplier)
        }

        override fun stop() {
            hardware.intakeBottom.stop()
            hardware.intakeTop.stop()
        }
    }

    fun align() =
        align(0.deg)

    fun ralign(angle: Angle) =
        align(hardware.imu.orientation.x + angle)

    fun align(angle: Angle) {
        val deg = angle.deg.value

        while (abs(hardware.imu.orientation.x.deg.value - deg) > 2)
            rotate(.3.mps * sign(hardware.imu.orientation.x.deg.value - deg))

        stop()
    }

    suspend fun accel(direction: Angle, distance: Distance, speed: Velocity): Bot {
//        move(direction, distance, speed)
//        return this

        val initial = robot.position

        val minSpeedStart = (velocity * Config.minSpeedStart).mps.value
        val minSpeedEnd = (velocity * Config.minSpeedEnd).mps.value

        val accelDistance = Config.accel
        val decelDistance = Config.decel

        val dr = 1.145
        val td = distance * dr

        val sp = speed.mps.value

        if (sp < minSpeedStart || td < accelDistance + decelDistance) {
            move(direction, td, speed)
            return this
        }

        do {
            DynamicPositioning.updateLinear(robot.positioning)
            val d = initial distance robot.position

            move(direction, when {
                d <= accelDistance ->            d / accelDistance * (sp - minSpeedStart) + minSpeedStart
                d <= td - decelDistance -> sp
                else ->                          (td - d) / decelDistance * (sp - minSpeedEnd) + minSpeedEnd
            }.mps)
        } while (d <= td)

        stop()
        return this
    }

    suspend fun accel(direction: Angle, distance: Distance, speed: Double): Bot =
        accel(direction, distance, velocity * speed)

    interface Shooter {
        fun revup()
        fun revdown()
        fun shoot()
        fun shoot(n: Int)
        fun cshoot()

        fun high()
        fun power()
    }

    interface Wobble {
        fun down()
        fun up()
        fun downAsync()
        fun upAsync()
        fun release()
        fun lock()
        fun releaseAsync()
        fun lockAsync()
    }

    interface Stick {
        fun down()
        fun up()

        operator fun invoke(down: Boolean) = if (down)
            down()
        else
            up()
    }

    interface Intake {
        fun intake()
        fun outtake()
        fun stop()

        fun intake(multiplier: Double)
        fun outtake(multiplier: Double)
    }
}

fun OperationMode.bot(odometry: Boolean = true): Bot {
    val hardware = Hardware(this) {{
        // Odometers
        y1 = Motor.get("m1", ticksPerDeg = Config.yEncoderTicks).encoder.invert().asDistanceEncoder(4.cm).zero()
        y2 = Motor.get("m5", ticksPerDeg = Config.yEncoderTicks).encoder.invert().asDistanceEncoder(4.cm).zero()
        x  = Motor.get("m6", ticksPerDeg = Config.xEncoderTicks).encoder.asDistanceEncoder(4.cm).zero()

        // Motors
        flywheel = Motor.get("m5", 5800.revpm)
        intakeBottom = Motor.get("m6", 312.revpm)
        wobble = Motor.get("m7", 235.revpm)
        intakeTop = Motor.get("m8", 312.revpm)

        // Servos
        shoot = Servo.get("s1")
        gripper = Servo.get("s2")
        variableShooter = Servo.get("s3")
        stick = Servo.get("s4")

        // Drive Motors
        m1 = Motor.get("m1", 435.revpm, 10.cm)
        m2 = Motor.get("m2", 435.revpm, 10.cm)
        m3 = Motor.get("m3", 435.revpm, 10.cm)
        m4 = Motor.get("m4", 435.revpm, 10.cm)

        m1.halt = Motor.HaltBehavior.Brake
        m2.halt = Motor.HaltBehavior.Brake
        m3.halt = Motor.HaltBehavior.Brake
        m4.halt = Motor.HaltBehavior.Brake

        // Imu
        imu = FImu.get("imu").init(
            accelerationUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC,
            angleUnit = BNO055IMU.AngleUnit.DEGREES
        )
    }}

    val robot = robot {
        using drive hardware.drive

        using positioning if (odometry) {
            val (y1, y2, x) = hardware.odometers
            TripleOdometry(y1, y2, x, 10.cm)
        } else {
            NoPositioning(true)
        }
    }

    return Bot(this, hardware, robot)
}

class Hardware {
    lateinit var y1: DistanceEncoder
    lateinit var y2: DistanceEncoder
    lateinit var x: DistanceEncoder

    lateinit var m1: Motor
    lateinit var m2: Motor
    lateinit var m3: Motor
    lateinit var m4: Motor

    lateinit var flywheel: Motor
    lateinit var intakeBottom: Motor
    lateinit var wobble: Motor
    lateinit var intakeTop: Motor

    lateinit var shoot: Servo
    lateinit var gripper: Servo
    lateinit var variableShooter: Servo
    lateinit var stick: Servo

    lateinit var imu: FImu

    val odometers get() = Triple(y1, y2, x)
    val drive: Drive get() = MecanumCopyJank(m4.invert(), m3.invert(), m1, m2)

    companion object {
        operator fun invoke(op: OperationMode, fn: OperationMode.() -> Hardware.() -> Unit): Hardware {
            val h = Hardware()
            fn(op)(h)
            return h
        }
    }
}