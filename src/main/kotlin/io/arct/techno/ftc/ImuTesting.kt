package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.units.cm
import io.arct.techno.ftc.util.robot

@OperationMode.Register(OperationMode.Type.Operated, "IMU Debug")
class ImuTesting : OperationMode() {
    private val robot = robot(true)

    private val motor = Motor.get("m1", ticksPerDeg = 256.0).encoder.invert().asDistanceEncoder(4.cm).zero()

    override suspend fun loop() {
//        log.add(motor.position.toString()).update()

        log
            .add("x: ${robot.position.x}")
            .add("y: ${robot.position.y}")
            .add("a: ${robot.rotation}")
            .update()
    }
}