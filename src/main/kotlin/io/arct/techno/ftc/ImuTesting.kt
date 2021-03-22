package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.hardware.sensors.FImu
import io.arct.rl.extensions.round
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.units.Angle
import io.arct.rl.units.cm
import io.arct.rl.units.deg
import io.arct.rl.units.mps
import io.arct.techno.ftc.jank.JankMoveTo
import io.arct.techno.ftc.util.imu
import io.arct.techno.ftc.util.odometers
import io.arct.techno.ftc.util.robot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OperationMode.Register(OperationMode.Type.Operated, "IMU Debug")
class ImuTesting : LinearOperationMode() {
    private val robot = robot(true)
    private val to = JankMoveTo(robot, dt = 10.cm, at = 10.deg)

    init {
        GlobalScope.launch {
            while (true)
                log.add("(${robot.position.x}, ${robot.position.y}) @ ${robot.rotation}").update()
        }
    }

    override suspend fun run() {
        to(100.cm, 100.cm, 1.mps)
    }
}