package com.example.mindfullexpenses.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.mindfullexpenses.core.autotrack.NotificationIngestionManager
import com.example.mindfullexpenses.core.autotrack.parser.SmsParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var smsParser: SmsParser
    @Inject lateinit var ingestionManager: NotificationIngestionManager

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val bundle: Bundle = intent.extras ?: return
        val pdus = bundle["pdus"] as? Array<*> ?: return
        val format = bundle.getString("format")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (pdu in pdus) {
                    val message = SmsMessage.createFromPdu(pdu as ByteArray, format)
                    val parsed = smsParser.parse(
                        address = message.displayOriginatingAddress,
                        body = message.displayMessageBody,
                        timestampMillis = message.timestampMillis
                    ) ?: continue

                    ingestionManager.ingestParsed(parsed)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to parse SMS", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}


