package com.fcc.biblioteca

import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.PermissionChecker
import android.Manifest

object SmsHelper {
    fun sendExpirySms(phone: String, bookTitle: String, expiryDate: String) {
        val message = "Biblio-Teca: Hola! Te recordamos que tu prestamo de '$bookTitle' expira el $expiryDate. Por favor devuélvelo a tiempo."
        executeSend(phone, message)
    }

    fun sendConfirmationSms(context: android.content.Context, phone: String, bookTitle: String) {
        // Verificar permiso rápido
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PermissionChecker.PERMISSION_GRANTED) {
            val message = "Biblio-Teca: ¡Felicidades! Tu prestamo de '$bookTitle' ha sido registrado. Tienes 14 dias para disfrutarlo."
            executeSend(phone, message)
        }
    }

    private fun executeSend(phone: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            Log.d("SmsHelper", "SMS enviado a $phone: $message")
        } catch (e: Exception) {
            Log.e("SmsHelper", "Error al enviar SMS: ${e.message}")
        }
    }
}
