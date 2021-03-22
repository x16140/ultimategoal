package io.arct.techno.ftc.jank

import io.arct.rl.hardware.sensors.DistanceEncoder
import io.arct.rl.hardware.sensors.Imu
import io.arct.rl.robot.position.OdometryPositioning
import io.arct.rl.robot.position.Positioning
import io.arct.rl.units.*
import kotlin.concurrent.thread
import kotlin.math.*

class JankPositioning(
    val y1: DistanceEncoder,
    val y2: DistanceEncoder,
    val x: DistanceEncoder,

    val r: Distance,
    val rb: Distance
) : Positioning() {
    override var position: Coordinates = Coordinates(0.cm, 0.cm)
        private set

    override var rotation: Angle = 0.deg

    private var thread: Thread? = null

    private var py1 = y1.position
    private var py2 = y2.position
    private var px = x.position

    init {
        zero()
    }

    fun spawn(): JankPositioning {
        stop()

        thread = thread(start = true) {
            while (true) {
                val dl = (y1.position - py1).cm.value
                val dr = (y2.position - py2).cm.value
                val db = (x.position - px).cm.value

                val dt = (dr - dl) / (r.cm.value * 2)

                val (dx, dy) = if (dt == .0) {
                    val dx = db
                    val dy = (dl + dr) / 2

                    Pair(dx, dy)
                } else {
                    val rt = (r.cm.value * (dl + dr)) / (dr - dl)
                    val rs = (db / dt) - rb.cm.value

                    val dx = rt * (cos(dt) - 1) + rs * sin(dt)
                    val dy = rt * sin(dt) + rs * (1 - cos(dt))

                    Pair(dx, dy)
                }

                val (cx, cy) = position
                position = Coordinates(cx + dx.cm, cy + dy.cm)

                rotation += dt.rad.deg

                py1 = y1.position
                py2 = y2.position
                px = x.position
            }
        }

        return this
    }

    fun stop(): JankPositioning {
        thread?.interrupt()
        return this
    }

    override fun zero(): JankPositioning {
        y1.zero()
        y2.zero()
        x.zero()

        py1 = y1.position
        py2 = y2.position
        px = x.position
        position = Coordinates(0.cm, 0.cm)

        return this
    }
}
