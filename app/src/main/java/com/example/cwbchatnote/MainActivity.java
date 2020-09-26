package com.example.cwbchatnote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Vibrator;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID ="Main" ;
    private static Context context;
    private String loginValue;
    private int checkValue=0; //temp variable to be deleted!
    private TextView editName;
    private TextView tempView; //temp view to be deleted!
    private Button circle_button;
    private  PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
       wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.FULL_WAKE_LOCK,
                "WebChatNoteApp::LoginWakelockTag");
        wakeLock.acquire();



        //register service broadcast to this activity with intent filter and call mMessageReceiver to execute actions
        LocalBroadcastManager.getInstance( MainActivity.context).registerReceiver(
                mMessageReceiver, new IntentFilter("LoginStatusCheck"));

            //start service periodically
           // createNotificationChannel();
            CheckStatus();
           circle_button=(Button) findViewById(R.id.circle_button);
        circle_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.visualvicinity.com/reactchat/"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
           editName  = (TextView) findViewById(R.id.notification);




    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String numOfLogins = intent.getStringExtra("Status");
            String userData = intent.getStringExtra("Data");
            Log.d("MESSAGE","SERVICE MESSAGE: "+numOfLogins);

            if(checkValue==1){ loginValue=numOfLogins;}else if(checkValue>1 && loginValue !=numOfLogins){
                loginValue=numOfLogins;
                Log.d("NEWLOGIN","NEWLOGIN: "+numOfLogins);
                editName.setText(userData);
                tempView.setText("Check status called X" + checkValue);
                createNotificationChannel(userData);

            }
        }

    };

    protected void CheckStatus ()  {


        wakeLock.acquire();

        checkValue++;
        tempView  = (TextView) findViewById(R.id.tempView);
        tempView.setText("Check status called X" + checkValue);
        Log.d("SERVICE","CHECKVALUE IS: "+String.valueOf(checkValue));


        Log.d("SERVICE","SERVICE START COMMAND");
        Intent i = new Intent(context, WebChatNoteService.class);
        i.putExtra("KEY1", "START");


        context.startService(i);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                stopCheckStatus();
            }
        }, 500);   //5 seconds

    }



    private void stopCheckStatus(){
        wakeLock.acquire();
    Log.d("SERVICE","SERVICE STOP COMMAND");
    Intent i = new Intent(context, WebChatNoteService.class);
    i.putExtra("KEY2", "stop check");

    context.stopService(i);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                CheckStatus ();
            }
        }, 500);   //5 seconds

}

    private void createNotificationChannel(String userData) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            //call notification builder
            noteVibration(userData);
        }
    }





private void noteVibration(String userData){

    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.FULL_WAKE_LOCK,
            "WebChatNoteApp::LoginWakelockTag");
    wakeLock.acquire();

   // PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
    KeyguardManager km = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
    final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
    kl.disableKeyguard();



    Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
    // Vibrate for 500 milliseconds
   v.vibrate(500);


    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)

            .setSmallIcon(R.drawable.ic_tau)
            .setContentTitle("New Login!")
            .setContentText(userData)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(defaultSoundUri)
            .setWhen(System.currentTimeMillis())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);;
            //builder.setVibrate(new long[] { 500, 500});

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
    int notificationId=8;
    notificationManager.notify(notificationId, builder.build());


}


}