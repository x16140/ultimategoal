package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.robot.position.NoPositioning
import io.arct.rl.robot.position.TripleOdometry
import io.arct.rl.robot.robot
import io.arct.rl.units.Angle
import io.arct.rl.units.cm
import io.arct.rl.units.m
import io.arct.rl.units.mps
import io.arct.techno.ftc.util.CalibrationData
import io.arct.techno.ftc.util.PersistentObject
import io.arct.techno.ftc.util.mecanum

@OperationMode.Register(OperationMode.Type.Autonomous, "Autonomous")
class Auto : LinearOperationMode() {
    private val s3 by Servo

    private val robot: Robot = robot {
        using drive       mecanum(this@Auto)
        using positioning TripleOdometry(
            Motor.get("m1").encoder.asDistanceEncoder(4.cm), // y right
            Motor.get("m6").encoder.asDistanceEncoder(4.cm), // y left
            Motor.get("m5").encoder.asDistanceEncoder(4.cm), // x
            10.cm
        )
    }

    private val calibration: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    override suspend fun run() {
        s3.position = calibration.shooterPower

//        while (true) {
            robot.move(Angle.Forward, 10.m, 1.mps)
            log.add(robot.position.toString()).add(robot.rotation.toString()).update()
//        }
    }
}