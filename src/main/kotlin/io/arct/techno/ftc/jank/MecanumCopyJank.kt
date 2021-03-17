package io.arct.techno.ftc.jank

import io.arct.rl.eventloop.Program
import io.arct.rl.extensions.normalize
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.navigation.DirectedPath
import io.arct.rl.robot.drive.Drive
import io.arct.rl.robot.position.DynamicPositioning
import io.arct.rl.robot.position.TripleOdometry
import io.arct.rl.units.*
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos

class MecanumCopyJank(
    vararg val motors: Motor,
    var invert: Boolean = false,
    val program: Program? = null
) : Drive() {
    override val velocity: Velocity = motors[0].velocity

    override fun move(direction: Angle, speed: Velocity): MecanumCopyJank {
        val dir = if (invert) -direction.deg else direction.deg

        val a = sin((dir + 45.deg).rad.value)
        val b = cos((dir + 45.deg).rad.value)

        val (x, y) = Double.normalize(a, b, upscale = true).map { speed * it }

        motors[0].power(x)
        motors[1].power(y)
        motors[2].power(x)
        motors[3].power(y)

        DynamicPositioning.updateLinear(robot.positioning)
        return this
    }

    override fun turn(speed: Velocity, rotationSpeed: Velocity): MecanumCopyJank {
        val velocity = motors[0].velocity

        val a = speed + if (rotationSpeed > 0.cmps) rotationSpeed else 0.0.cmps
        val b = speed - if (rotationSpeed < 0.cmps) rotationSpeed else 0.0.cmps

        val (l, r) = Double.normalize(a / velocity, b / velocity)

        motors[0].power(l)
        motors[1].power(l)
        motors[2].power(r)
        motors[3].power(r)

        DynamicPositioning.updateAngular(robot.positioning)
        return this
    }

    override suspend fun move(direction: Angle, distance: Distance, speed: Velocity): MecanumCopyJank {
        val initial = robot.position
        move(direction, speed)

        while (initial distance robot.position <= distance && (program == null || program.active)) {
            DynamicPositioning.updateLinear(robot.positioning)
        }

        stop()

        return this
    }

    override suspend fun move(path: DirectedPath): MecanumCopyJank {
        val initial = robot.position

        do {
            val pos = initial distance robot.position
            val speed = path.path(pos)

            move(path.direction, speed)
            delay(10)

            DynamicPositioning.updateLinear(robot.positioning)
        } while (speed.value != 0.0 && (program == null || program.active))

        stop()
        return this
    }

    override suspend fun rotate(angle: Angle, speed: Velocity): MecanumCopyJank {
        val initial = robot.rotation

        rotate(speed)

        while (abs((robot.rotation - initial).deg.value) <= angle.deg.value && (program == null || program.active))
            DynamicPositioning.updateAngular(robot.positioning)

        stop()
        return this
    }

    override fun rotate(speed: Velocity): MecanumCopyJank {
        motors[0].power(speed)
        motors[1].power(speed)
        motors[2].power(-speed)
        motors[3].power(-speed)

        DynamicPositioning.updateAngular(robot.positioning)
        return this
    }
}