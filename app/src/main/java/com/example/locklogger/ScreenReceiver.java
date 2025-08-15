package com.example.locklogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        String tag;

        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                tag = "LOCK";
                break;
            case Intent.ACTION_SCREEN_ON:
                tag = "UNLOCK";
                break;
            case Intent.ACTION_USER_PRESENT:
                tag = "LOGIN";
                break;
            default:
                return;
        }

        LogUtils.appendLogAsync(context, tag);
    }
}
