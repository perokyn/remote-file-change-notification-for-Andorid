package com.example.cwbchatnote;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class WebChatNoteService extends Service {
    private static Context context;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private boolean isRunning  = false;
    private ScheduledExecutorService scheduleTaskExecutor;




    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {


                // Create a URL for the desired page
                URL url = new URL("http://www.visualvicinity.com/reactchat/output.txt"); //My text file location

                int counter=0;
                int counter2=0;
                String str;
                String newLogin="";



                    //First open the connection
                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                  //  conn.setConnectTimeout(60000); // timing out in a minute
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                            while ((str = in.readLine()) != null) {
                                counter++;
                              // Log.d("TEXT FILE CONTENT",str +"Counter"+counter);
                               newLogin=str;
                            }  in.close();
                            if(counter>counter2){
                                sendMessageToActivity(String.valueOf(counter),newLogin);
                                counter2=0;
                            }else{counter2=counter;
                                counter=0;}









            } catch (Exception e) {
                Log.d("MyTag",e.toString());
            }

            stopSelfResult(msg.arg1);

        }
    }



    private static void sendMessageToActivity(String numOfLogins,String data) {
        Log.d("sendMessageToActivity","SEND MESSAGE CALLED: "+numOfLogins);
        Intent intent = new Intent("LoginStatusCheck");
        intent.putExtra("Status",numOfLogins);
        intent.putExtra("Data",data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }





    @Override
    public void onCreate() {

       WebChatNoteService.context = getApplicationContext();


        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Bundle extras=intent.getExtras();
        String newString= extras.getString("KEY1");
        Log.d("Message From Activity"," MESSAGE RETREIVED: "+newString);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return Service.START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
       // Toast.makeText(this, "chat/web notification service stopped", Toast.LENGTH_SHORT).show();
        isRunning = false;
    }





}
