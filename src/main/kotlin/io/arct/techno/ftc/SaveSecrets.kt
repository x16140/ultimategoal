package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.techno.ftc.util.PersistentObject
import io.arct.techno.ftc.util.Secret

@OperationMode.Register(OperationMode.Type.Autonomous, "Save Secrets")
class SaveSecrets : LinearOperationMode() {
    override suspend fun run() {
//        PersistentObject.save(Secret(
//            vuforia = "<key>"
//        ), "/sdcard/secrets.dat")
    }
}