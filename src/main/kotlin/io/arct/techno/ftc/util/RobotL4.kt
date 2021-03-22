package io.arct.techno.ftc.util

import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.robot.Robot
import io.arct.rl.robot.drive.MecanumDrive
import io.arct.rl.robot.position.NoPositioning
import io.arct.rl.robot.position.TripleOdometry
import io.arct.rl.units.cm
import io.arct.rl.units.revpm

val OperationMode.odometersL4 get() = Triple(
        Motor.get("m1", ticksPerDeg = 13.2159).encoder.invert().asDistanceEncoder(4.cm), // y right
        Motor.get("m5", ticksPerDeg = 13.2159).encoder.invert().asDistanceEncoder(4.cm), // y left
        Motor.get("m6", ticksPerDeg = 1.4706).encoder.asDistanceEncoder(4.cm) // x
)

fun OperationMode.robotL4(odometry: Boolean = true): Robot = io.arct.rl.robot.robot {
    using drive mecanumL4

    using positioning if (odometry) {
        val (y1, y2, x) = odometersL4
        TripleOdometry(y1, y2, x, 10.cm)
    } else {
        NoPositioning(true)
    }
}

val OperationMode.mecanumL4: MecanumDrive get() {
    val m1 = Motor.get("m1", 435.revpm, 10.cm)
    val m2 = Motor.get("m2", 435.revpm, 10.cm)
    val m3 = Motor.get("m3", 435.revpm, 10.cm)
    val m4 = Motor.get("m4", 435.revpm, 10.cm)

    m1.halt = Motor.HaltBehavior.Brake
    m2.halt = Motor.HaltBehavior.Brake
    m3.halt = Motor.HaltBehavior.Brake
    m4.halt = Motor.HaltBehavior.Brake

    return MecanumDrive(m4.invert(), m3.invert(), m1, m2)
}