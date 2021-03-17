package io.arct.techno.ftc.jank

import io.arct.ftc.hardware.sensors.FImu
import io.arct.rl.hardware.sensors.DistanceEncoder
import io.arct.rl.robot.position.DynamicPositioning
import io.arct.rl.robot.position.IPositioning
import io.arct.rl.robot.position.TripleOdometry
import io.arct.rl.units.*
import org.firstinspires.ftc.robotcore.external.navigation.Orientation
import kotlin.math.PI

class ImuPositioning(
        private val y1: DistanceEncoder,
        private val y2: DistanceEncoder,
        private val x: DistanceEncoder,
        private val imu: FImu,
        private val imuAxis: (Orientation) -> Float,
        private val invert: Boolean,
        private val diameter: Distance
) : DynamicPositioning() {

    private val angularRatio: Angle = ((y1.diameter.cm * 2 * PI) / diameter.cm).rad

    private var y1d: Distance = y1.position
    private var y1a: Angle = y1.angle
    private var y2d: Distance = y2.position
    private var y2a: Angle = y2.angle
    private var x1d: Distance = x.position

    override fun zero(): ImuPositioning {
        y1.zero()
        y2.zero()
        x.zero()
        push()
        return this
    }

    override fun updateLinear() {
        val dy1 = y1.position - y1d
        val dy2 = y2.position - y2d
        val dx1 = x.position - x1d

        val dy = (dy1 + dy2) / 2
        val dx = dx1

        position = Coordinates(position.x + dx, position.y + dy)
        push()
    }

    override fun updateAngular() {
        rotation = imuAxis(imu.orientation).deg * if (invert) -1 else 1
        push()
    }

    private fun push() {
        y1d = y1.position
        y1a = y1.angle
        y2d = y2.position
        y2a = y2.angle
        x1d = x.position
    }

}