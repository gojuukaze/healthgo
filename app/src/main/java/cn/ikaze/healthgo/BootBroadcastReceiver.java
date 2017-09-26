package cn.ikaze.healthgo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import cn.ikaze.healthgo.step.StepService;

/**
 * Created by gojuukaze on 16/8/20.
 * Email: i@ikaze.uu.me
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("conf", context.MODE_PRIVATE);
        boolean temp=sharedPreferences.getBoolean("switch_on", false);
        if (!temp)
            return;
        Intent serviceIntent = new Intent(context, StepService.class);
        serviceIntent.putExtra("restart",intent.getAction());
        context.startService(serviceIntent);
    }
}
