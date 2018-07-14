package com.example.lawrene.selfwithdrawal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Array;

/**
 * Created by lawrene on 6/27/18.
 */

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            String msgBody;

            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msgBody = msgs[i].getMessageBody();

                        Log.i("TAG", msg_from+msgBody);

                        // A custom Intent that will used as another Broadcast
                        Intent in = new Intent(context,MainActivity.class).
                                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).
                                putExtra("sender", msg_from).
                                putExtra("body", msgBody);

                        //You can place your check conditions here(on the SMS or the sender)
                        //and then send another broadcast
                        context.startActivity(in);

//                        int [] n = {};

                    }
                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                }
            }
        }


    }



}
