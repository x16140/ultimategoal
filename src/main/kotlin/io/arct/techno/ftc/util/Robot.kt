package io.arct.techno.ftc.util

import com.qualcomm.hardware.bosch.BNO055IMU
import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.hardware.sensors.FImu
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.robot.Robot
import io.arct.rl.robot.position.ImuPositioning
import io.arct.rl.robot.position.NoPositioning
import io.arct.rl.robot.position.OdometryPositioning
import io.arct.rl.units.*
import io.arct.techno.ftc.jank.JankPositioning
import io.arct.techno.ftc.jank.MecanumCopyJank

val OperationMode.odometers get() = Triple(
        Motor.get("m1", ticksPerDeg = 12.0).encoder.invert().asDistanceEncoder(4.cm), // y right
        Motor.get("m5", ticksPerDeg = 12.0).encoder.invert().asDistanceEncoder(4.cm), // y left
        Motor.get("m6", ticksPerDeg = 12.0).encoder.asDistanceEncoder(4.cm) // x
)

val OperationMode.imu get() = FImu.get("imu").init(
    accelerationUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC,
    angleUnit = BNO055IMU.AngleUnit.DEGREES
)

fun OperationMode.robot(odometry: Boolean = true): Robot = io.arct.rl.robot.robot {
    using drive mecanum

    using positioning if (odometry) {
        val (y1, y2, x) = odometers
//        OdometryPositioning(y1, y2, x, imu, angle = { -it.x }).spawn()
        JankPositioning(y1, y2, x, r = 19.5.cm, rb = 15.2.cm).spawn()
    } else {
        NoPositioning(true)
    }
}

val OperationMode.mecanum: MecanumCopyJank get() {
    val m1 = Motor.get("m1", 435.revpm, 10.cm)
    val m2 = Motor.get("m2", 435.revpm, 10.cm)
    val m3 = Motor.get("m3", 435.revpm, 10.cm)
    val m4 = Motor.get("m4", 435.revpm, 10.cm)

    m1.halt = Motor.HaltBehavior.Brake
    m2.halt = Motor.HaltBehavior.Brake
    m3.halt = Motor.HaltBehavior.Brake
    m4.halt = Motor.HaltBehavior.Brake

    return MecanumCopyJank(m4.invert(), m3.invert(), m1, m2)
}