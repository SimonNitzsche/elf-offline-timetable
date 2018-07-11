package tk.cs8898.elfofflinett.receiver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import tk.cs8898.elfofflinett.services.NotificationService;

public class AutostartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                assert jobScheduler != null;
                ComponentName serviceComponent = new ComponentName(context, NotificationService.ScheduledNotificationService.class);

                JobInfo.Builder builder = new JobInfo.Builder(NotificationService.ScheduledNotificationService.INIT_JOB_ID, serviceComponent);
                builder.setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false);
                jobScheduler.schedule(builder.build());
            } else {
                NotificationService.startActionInitNotification(context);
            }
        }
    }
}
