package io.arct.techno.ftc.util

import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.robot.drive.MecanumDrive
import io.arct.rl.units.cm
import io.arct.rl.units.revpm

fun mecanum(op: OperationMode): MecanumDrive {
    val fn: OperationMode.() -> Pair<Pair<Motor, Motor>, Pair<Motor, Motor>> = {
        val m1 = Motor.get("m1", 435.revpm, 10.cm)
        val m2 = Motor.get("m2", 435.revpm, 10.cm)
        val m3 = Motor.get("m3", 435.revpm, 10.cm)
        val m4 = Motor.get("m4", 435.revpm, 10.cm)

        Pair(Pair(m1, m2), Pair(m3, m4))
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