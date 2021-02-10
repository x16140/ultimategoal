package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.units.Angle
import io.arct.rl.units.m
import io.arct.rl.units.mps
import io.arct.techno.ftc.cv.RingDetector
import io.arct.techno.ftc.util.CalibrationData
import io.arct.techno.ftc.util.PersistentObject
import io.arct.techno.ftc.util.Secret
import io.arct.techno.ftc.util.robot

@OperationMode.Register(OperationMode.Type.Autonomous, "Autonomous")
class Auto : LinearOperationMode() {
    private val s3 by Servo

    private val secrets: Secret = PersistentObject.load("/sdcard/secrets.dat")
    private val calibration: CalibrationData = PersistentObject.load("/sdcard/calibration.dat")

    private val robot: Robot = robot(this)
    private val detector = RingDetector(this, secrets.vuforia)

    override suspend fun init() {
        s3.position = calibration.shooterPower
    }

    override suspend fun run() {
        val state = detector.scan()

        robot.move(Angle.Forward, 10.m, 1.mps)
    }
}