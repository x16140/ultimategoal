package io.arct.techno.ftc.jank


import io.arct.rl.extensions.normalize
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.sensors.DistanceEncoder
import io.arct.rl.robot.Robot
import io.arct.rl.robot.drive.MecanumDrive
import io.arct.rl.robot.position.DynamicPositioning
import io.arct.rl.units.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class JankMoveMec(private val robot: Robot, var state: Boolean) {
    fun move(direction: Angle, speed: Velocity, rotationSpeed: Velocity) {
        if (state) {
            robot.move(direction, speed)
        } else {
            robot.rotate(rotationSpeed)
        }

        state = !state
    }
}

class JankAdjustOdometry(private val robot: Robot, private val drive: MecanumDrive, odometers: Triple<DistanceEncoder, DistanceEncoder, DistanceEncoder>) {
    private val y1 = odometers.first
    private val y2 = odometers.second
    private val x = odometers.third

    fun reset() {
        y1.reset()
        y2.reset()
        x.reset()
    }

    suspend fun adjust(direction: Angle, target: Distance) {
        while (y1.position > target && y2.position > target)
            robot.move(direction + 180.deg, target, 0.2.mps)

        while (y1.position > y2.position)
            robot.turn(0.mps, 0.2.mps)

        while (y2.position > y1.position)
            robot.turn(0.mps, (-0.2).mps)
    }

    fun turn(motors: List<Motor>, speed: Velocity, rotationSpeed: Velocity) {
        val velocity = motors[0].velocity

        val a = speed + if (rotationSpeed > 0.cmps) rotationSpeed else 0.0.cmps
        val b = speed - if (rotationSpeed < 0.cmps) rotationSpeed else 0.0.cmps

        val (l, r) = Double.normalize(a / velocity, b / velocity)

        motors[0].power(l)
        motors[1].power(l)
        motors[2].power(r)
        motors[3].power(r)

        DynamicPositioning.updateAngular(robot)
    }
}