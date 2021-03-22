package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.techno.ftc.util.CalibrationData
import io.arct.techno.ftc.util.PersistentObject
import io.arct.techno.ftc.util.Secret

@OperationMode.Register(OperationMode.Type.Autonomous, "Calibration Reset")
class SaveSecrets : LinearOperationMode() {
    override suspend fun run() {
//        PersistentObject.save(Secret(
//            vuforia = "<key>"
//        ), "/sdcard/secrets.dat")

        PersistentObject.save(CalibrationData(
                shooterHigh = 0.5,
                shooterPower = 0.5,
                shootDelay = 250L
        ), "/sdcard/calibration.dat")
    }
}