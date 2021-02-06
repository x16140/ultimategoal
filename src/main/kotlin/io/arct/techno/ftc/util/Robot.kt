package io.arct.techno.ftc.util

import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.sensors.DistanceEncoder
import io.arct.rl.robot.Robot
import io.arct.rl.robot.drive.MecanumDrive
import io.arct.rl.robot.position.NoPositioning
import io.arct.rl.robot.position.TripleOdometry
import io.arct.rl.units.cm
import io.arct.rl.units.revpm

fun robot(op: OperationMode, odometry: Boolean = true): Robot = io.arct.rl.robot.robot {
    using drive mecanum(op)

    if (odometry) {
        val fn: OperationMode.() -> Pair<Pair<DistanceEncoder, DistanceEncoder>, DistanceEncoder> = {
            val a = Motor.get("m1").encoder.invert().asDistanceEncoder(4.cm) // y right
            val b = Motor.get("m6").encoder.invert().asDistanceEncoder(4.cm) // y left
            val c = Motor.get("m5").encoder.asDistanceEncoder(4.cm) // x

            a to b to c
        }

        val (a, x) = fn(op)
        val (y1, y2) = a

        using positioning TripleOdometry(y1, y2, x, 10.cm)
    } else {
        using positioning NoPositioning(true)
    }
}

fun mecanum(op: OperationMode): MecanumDrive {
    val fn: OperationMode.() -> Pair<Pair<Motor, Motor>, Pair<Motor, Motor>> = {
        val m1 = Motor.get("m1", 435.revpm, 10.cm)
        val m2 = Motor.get("m2", 435.revpm, 10.cm)
        val m3 = Motor.get("m3", 435.revpm, 10.cm)
        val m4 = Motor.get("m4", 435.revpm, 10.cm)

        (m1 to m2) to (m3 to m4)
    }

    val (a, b) = fn(op)
    val (m1, m2) = a
    val (m3, m4) = b

    m1.halt = Motor.HaltBehavior.Brake
    m2.halt = Motor.HaltBehavior.Brake
    m3.halt = Motor.HaltBehavior.Brake
    m4.halt = Motor.HaltBehavior.Brake

    return MecanumDrive(m4.invert(), m3.invert(), m1, m2)
}