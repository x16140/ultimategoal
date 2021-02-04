package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.eventloop.OperationMode.Type
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.robot.position.NoPositioning
import io.arct.rl.robot.robot
import io.arct.rl.units.*
import io.arct.techno.ftc.util.mecanum

@OperationMode.Register(Type.Autonomous, "Auto (League 3)")
class AutoL3 : LinearOperationMode() {
    private val shooter = Motor.get("m5", 5800.revpm)
    private val s1 by Servo
    private val s2 by Servo

    private val robot: Robot = robot {
        using drive       mecanum(this@AutoL3)
        using positioning NoPositioning(errorOnAccess = true)
    }

    val shooterPower = -1.0
    val shootDelay = 50L
    val shooterPositionA = 0.7
    val shooterPositionB = 1.0

    override suspend fun init() {
        s2.position = -0.8
    }

    override suspend fun run() {
        runToShoot()

        for (i in 1..4)
            shoot()

        Thread.sleep(1000L)

        revdown()

        Thread.sleep(3000L)

        runToLine()
    }

    private fun revup() =
        shooter.power(shooterPower)

    private fun revdown() =
        shooter.stop()

    private fun shoot() {
        Thread.sleep(1000)
        s1.position = shooterPositionA
        Thread.sleep(shootDelay)
        s1.position = shooterPositionB
    }

    private fun runToShoot() {
        robot.move(Angle.Forward, 1.mps)
        Thread.sleep(1500L)
        revup()
        Thread.sleep(1650L)
        robot.stop()
    }

    private fun runToLine() {
        robot.move(Angle.Forward, 1.mps)
        Thread.sleep(750L)
        robot.stop()
    }
}