package com.mfcompany.wakeupontime;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.mfcompany.wakeupontime.utilidades.Utilidades;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 26/11/2020.
 */

public class MyAlarmReceiver extends BroadcastReceiver {

    private final int NOTIFICATION_ID = 1010;
    private static final String CHANNEL_ID2 ="000" ;
    private NotificationManager notificationManager;
    private String Encabezado, Mensaje, Latitud, Longitud;
    private Calendar calendario;
    int HOUR, MINUTE, YEAR, MONTH, DAY;
    String timeSystem, dateSystem;
    private ConexionSQLiteHelper conexion;
    private SQLiteDatabase db;
    PendingIntent pendingtIntent;
    Context contextIntent;



    @Override
    public void onReceive(Context context, Intent intent) {
        Intent inTent = new Intent(context, MyTestService.class);
        context.startService(inTent);
        contextIntent = context;
        conexion = new ConexionSQLiteHelper(context);
        db = conexion.getReadableDatabase();

        Bundle Info = intent.getExtras();
        if (Info!=null){
            int Pass = Info.getInt("alarma");
            if (Pass == 1 )Validar(context);
        }
    }

    private boolean Validar(Context context){
        calendario = Calendar.getInstance();
        HOUR = calendario.get(java.util.Calendar.HOUR_OF_DAY);
        MINUTE = calendario.get(java.util.Calendar.MINUTE);
        YEAR = calendario.get(java.util.Calendar.YEAR);
        MONTH = calendario.get(java.util.Calendar.MONTH)+1;
        DAY = calendario.get(java.util.Calendar.DAY_OF_MONTH);
        dateSystem = DAY+"-"+MONTH+"-"+YEAR;
        timeSystem = String.format("%02d:%02d",HOUR,MINUTE);
        Cursor cursor;
        String[] parametros = {dateSystem,timeSystem};

        try {
            cursor = db.rawQuery("SELECT "+ Utilidades.TABLA_ENCABEZADO+" , "+Utilidades.TABLA_MENSAJE+" , "+Utilidades.TABLA_LATITUD+" , "+Utilidades.TABLA_LONGITUD+" FROM "+Utilidades.TABLA_ALARMA +" WHERE "+Utilidades.TABLA_FECHA+"=? AND "+Utilidades.TABLA_HORA+"=?", parametros);
            cursor.moveToFirst();
            Encabezado = cursor.getString(0);
            Mensaje = cursor.getString(1);
            Latitud = cursor.getString(2);
            Longitud = cursor.getString(3);
            cursor.close();
            setPendingIntent();
            triggerNotification(context, Encabezado, Mensaje,  Latitud, Longitud);
            createNotificationChannel(context, Encabezado, Mensaje,  Latitud, Longitud);
            return true;
        }catch (Exception e){
            Log.i("ERROR VALIDAR MAR:", ""+e);
        }
        return false;
    }

    private void setPendingIntent(){
        Intent intent = new Intent(contextIntent, Activity_Home.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(contextIntent);
        stackBuilder.addParentStack(Activity_Home.class);
        stackBuilder.addNextIntent(intent);
        pendingtIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void triggerNotification(Context context, String encabezado, String mensaje, String latitud, String longitud) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long[] pattern = new long[]{0, 100, 1000,200,2000,1000,2000,200,1000,100};

        String CuerpoM = mensaje+"\nEn la Ubicacion: -> Lat: "+latitud+" Lon: "+longitud;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent)
                .setContentText(CuerpoM)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.alarmacolor))
                .setSmallIcon(R.drawable.alarma)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification5))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setVibrate(pattern);
        builder.setContentIntent(pendingtIntent);

        Notification notificacion = new NotificationCompat.BigTextStyle(builder)
                .setBigContentTitle(encabezado)
                .build();

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificacion);
    }

    private void createNotificationChannel(Context context,String encabezado, String mensaje, String latitud, String longitud) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = encabezado;
            String description = mensaje+"\nEn la Ubicacion: -> Lat: "+latitud+" Lon: "+longitud;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID2, name, importance);
            channel.setDescription(description);
            channel.setVibrationPattern(new long[]{0, 100, 1000,200,2000,1000,2000,200,1000,100});
            channel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification5),audioAttributes);
            notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
