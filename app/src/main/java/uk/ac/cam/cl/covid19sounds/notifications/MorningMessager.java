package uk.ac.cam.cl.covid19sounds.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.activities.MainActivity;
import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;
import uk.ac.cam.cl.covid19sounds.localisation.Localisation;

public class MorningMessager extends BroadcastReceiver {
//code to send morning message every 2 days to the user to complete their daily survey

    public static final int NOTIFICATION_ID = 5;
    private static final int REQUEST_CODE = 5;
    private static final String NOTIFICATION_CHANNEL_ID = "morning_message_channel";

    public MorningMessager() {
    }

    public MorningMessager(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        UserOnboardingPrefs prefs = UserOnboardingPrefs.getInstance(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        setNotificationChannel(notificationManager);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(REQUEST_CODE, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        //translate here as well
        String title;
        String message;
        switch (context.getResources().getConfiguration().locale.getDisplayLanguage())
        {
            case (Localisation.LANGUAGES_IT):
                title = "Buongiorno";
                message = "Completa il tuo sondaggio giornaliero per aiutare la ricerca COVID-19.";
                break;
            case (Localisation.LANGUAGES_DE):
                title = "Guten morgen!";
                message = "Füllen Sie Ihre tägliche Umfrage aus, um die COVID-19-Forschung zu unterstützen.";
                 break;
            case (Localisation.LANGUAGES_ES):
                title = "¡Buenos días!";
                message = "Complete su encuesta diaria para ayudar en la investigación de COVID-19.";
                break;
            case (Localisation.LANGUAGES_FR):
                title = "Bonne matinée";
                message = "Répondez à votre enquête quotidienne pour aider la recherche COVID-19.";
                break;
            case (Localisation.LANGUAGES_HI):
                 title = "शुभ प्रभात!";
                 message = "COVID-19 अनुसंधान में मदद करने के लिए अपने दैनिक सर्वेक्षण को पूरा करें";
                 break;
            case (Localisation.LANGUAGES_PT):
                title ="Bom Dia!";
                message = "Complete sua pesquisa diária para ajudar na pesquisa do COVID-19.";
                break;
            case (Localisation.LANGUAGES_RU):
                title ="Доброе утро!";
                message = "Заполните ежедневный опрос чтобы помочь в исследовании КОВИД-19.";
                break;
            case (Localisation.LANGUAGES_EL):
                title ="Καλημέρα!";
                message = "Παρακαλώ συμπληρώστε τις σύντομες ερωτήσεις για να βοηθήσετε την έρευνα κατά του COVID-19.";
                break;
            case (Localisation.LANGUAGES_RO):
                title ="Bună dimineața!";
                message = "Completați sondajul zilnic pentru a ajuta la cercetarea COVID-19.";
                break;
            case Localisation.LANGUAGES_ZH:
            case Localisation.LANGUAGES_ZH_SIMP:
            case Localisation.LANGUAGES_ZH_TRAD:
                title ="早上好!";
                message = "请您完成每日问卷来帮助新冠防治研究。";
                break;

            default:
                title = "Good morning!";
                message = "Complete your daily survey to help COVID-19 research.";
                break;
         }
        notification.setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        // send notification
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification.build());
        }
        // schedule next
        setMorningMessager(context, prefs.getMorningNotificationHour(), prefs.getMorningNotificationMinute());
    }

    public void setMorningMessager(Context context, int hour, int minute) {
        // Calculate time to set the message
        Calendar calendar1 = Calendar.getInstance(); // today
        calendar1.set(Calendar.HOUR_OF_DAY, hour);
        calendar1.set(Calendar.MINUTE, minute);
        calendar1.set(Calendar.SECOND, 0);

        long mills = calendar1.getTimeInMillis(); // The calendar now has the time that the message was supposed to be sent today
        if (mills <= System.currentTimeMillis()) { // If it was already meant to be sent
            calendar1.add(Calendar.DAY_OF_MONTH, 2);
            mills = calendar1.getTimeInMillis(); // Set for in 2 days
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("MORNING_MESSAGE");
        intent.setClass(context, MorningMessager.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, 0);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, mills, pendingIntent);
        }
    }

    // Configure the notification channel
    public void setNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Morning survey reminder", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}