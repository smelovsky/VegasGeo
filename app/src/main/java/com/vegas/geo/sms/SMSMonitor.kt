package com.vegas.geo.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import com.vegas.geo.ScannerMode
import com.vegas.geo.mainViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class SMSMonitor : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val myStatus = mainViewModel.getMyStatus()

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender = message.originatingAddress

                if (message.messageBody.trim() == "?" || message.messageBody.trim() == "*") {

                    if (message.messageBody.trim() == "*") {
                        if(mainViewModel.getAlarm()) {
                            mainViewModel.play()
                        }
                    }

                    val sms: SmsManager = SmsManager.getDefault()
                    sms.sendTextMessage(sender, null,
                        "#${mainViewModel.currentPoint.latitude},${mainViewModel.currentPoint.longitude}#${myStatus}",
                        null, null)

                    abortBroadcast()

                } else if (message.messageBody.trim().first() == '#') {

                    val index = message.messageBody.lastIndexOf("#")
                    if (index != -1) {
                        val array = message.messageBody.substring(1, index - 1).split(",")
                        if (array.size == 2) {
                            try {
                                mainViewModel.scannerPoint = GeoPoint(array.first().toDouble(), array.last().toDouble())

                                mainViewModel.scannerPoint = GeoPoint(array.first().toDouble(), array.last().toDouble())
                                mainViewModel.setMessage(message.messageBody.substring(index + 1))
                                mainViewModel.setScannerMode(ScannerMode.SHOW_MESSAGE)

                            } catch (e: Exception) {

                                mainViewModel.setScannerMode(ScannerMode.SHOW_ERROR)
                            }

                        }
                    }

                }
            }


        }
    }

}
