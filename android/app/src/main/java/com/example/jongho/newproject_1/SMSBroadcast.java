package com.example.jongho.newproject_1;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SMSBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
            Bundle bundle = intent.getExtras();
            Object message[] = (Object[]) bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[message.length];

            for(int i = 0; i < message.length; i++) {
                smsMessage[i] = SmsMessage.createFromPdu((byte[]) message[i]);
            }

            Date curDate = new Date(smsMessage[0].getTimestampMillis());
            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.KOREA);

            String originDate = mDateFormat.format(curDate);
            String origNumber = smsMessage[0].getOriginatingAddress();
            String Message = smsMessage[0].getMessageBody().toString();

            Intent showSMSIntent = new Intent(context, ContractsActivity.class);
            showSMSIntent.putExtra("originNum", origNumber);
            showSMSIntent.putExtra("smsDate", originDate);
            showSMSIntent.putExtra("originText", Message);

            showSMSIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(showSMSIntent);
        }
    }
}
