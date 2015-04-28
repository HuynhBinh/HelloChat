package com.es.hello.chat.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//this class ask the HelloMainService to run after user has reboot the device
public class BootCompletedReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {

	if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
	{	    
	    Intent pushIntent = new Intent(context, HelloMainService.class);
	    pushIntent.addCategory(HelloMainService.TAG);
	    context.startService(pushIntent);
	}
    }
}
