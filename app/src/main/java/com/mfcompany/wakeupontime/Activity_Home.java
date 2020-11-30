package com.mfcompany.wakeupontime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mfcompany.wakeupontime.entidades.ClassAlarma;
import com.mfcompany.wakeupontime.utilidades.Utilidades;
import android.location.Location;
import android.location.LocationListener;
import android.provider.Settings;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 10/09/2020.
 */

public class Activity_Home extends AppCompatActivity {
    private static final int CHANNEL_ID =000 ;
    private static final String CHANNEL_ID2 ="000" ;

    TextView Temperatura, Ciudad, Descripcion;
    ImageView IconoClima, IconoGrados, iconoTrafico;
    TextClock Hora;

    Double LatitudGPS;
    Double LongitudGPS;
    //String Latitud= "7.8971458"; String Longitud= "-72.5080387";

    String urlC ="http://api.openweathermap.org/data/2.5/weather?lat=";
    String ApiKeyC= "73bfe2d3e1813c219883a938949038aa";
    String QueryC;

    String urlT ="https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?point=";
    String ApiKeyT= "hHimuFXT0DtkVExQgufHCDNhMFU6r15D";
    String QueryT;

    int Vueltas=0;

    /*-------------------------------------*/
    private ConexionSQLiteHelper conexion;
    private SQLiteDatabase db;
    private Calendar calendario;
    int HOUR, MINUTE, YEAR, MONTH, DAY;
    String timeSystem, dateSystem;

    @RequiresApi (api = Build.VERSION_CODES.JELLY_BEAN_MR1)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__home);

        Hora = (TextClock) findViewById(R.id.Hora);
        Hora.setFormat12Hour("hh:mm a");

        conexion = new ConexionSQLiteHelper(this);
        db = conexion.getReadableDatabase();

        Temperatura = (TextView) findViewById(R.id.Temperatura);
        Ciudad = (TextView) findViewById(R.id.Ciudad);
        Descripcion = (TextView) findViewById(R.id.Descripcion);
        IconoClima = (ImageView) findViewById(R.id.IconoClima);
        IconoGrados = (ImageView) findViewById(R.id.IconoGrados);
        iconoTrafico = (ImageView) findViewById(R.id.iconoTrafico);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
        GetDatos();
    }

    /******************************************************************************************************/
    public void DateTime(){
        calendario = Calendar.getInstance();
        HOUR = calendario.get(java.util.Calendar.HOUR_OF_DAY);
        MINUTE = calendario.get(java.util.Calendar.MINUTE);
        YEAR = calendario.get(java.util.Calendar.YEAR);
        MONTH = calendario.get(java.util.Calendar.MONTH)+1;
        DAY = calendario.get(java.util.Calendar.DAY_OF_MONTH);
        dateSystem = DAY+"-"+MONTH+"-"+YEAR;
        timeSystem = String.format("%02d:%02d",HOUR,MINUTE);
    }

    public void GetDatos(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Vueltas++;
                        servicio();
                        if(Vueltas == 2){
                            GetDatosC();
                            GetDatosT();
                        }
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread hilo = new Thread(runnable);
        hilo.start();
    }

    public void servicio() {
        if (Validar()){
            Intent intent = new Intent(Activity_Home.this, MyAlarmReceiver.class);
            Bundle Info = new Bundle();
            Info.putInt("alarma",1);
            intent.putExtras(Info);
            PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(),0,intent,0);
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1+1000,pIntent);
        }
    }

    private boolean Validar(){
        DateTime();
        boolean Validacion = false;
        Cursor cursor;
        String[] parametros = {dateSystem,timeSystem};
        try {
            cursor = db.rawQuery("SELECT "+ Utilidades.TABLA_ENCABEZADO+" , "+Utilidades.TABLA_MENSAJE+" , "+Utilidades.TABLA_LATITUD+" , "+Utilidades.TABLA_LONGITUD+" FROM "+Utilidades.TABLA_ALARMA +" WHERE "+Utilidades.TABLA_FECHA+"=? AND "+Utilidades.TABLA_HORA+"=?", parametros);
            if (cursor.moveToFirst()){
                cursor.close();
                Validacion = true;
            }else {
                Validacion = false;
            }
        }catch (Exception e){
            Log.i("ERROR VALIDAR:", ""+e);
        }
        return Validacion;
    }

    /****************************************** location *************************************************/
    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setActivity_Home(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc) {
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);

                    System.out.println("Mi direccion es: "+ DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Clase Localizacion */
    public class Localizacion implements LocationListener {
        int contador =0;
        Activity_Home Activity_Home;
        public Activity_Home getActivity_Home() {
            return Activity_Home;
        }
        public void setActivity_Home(Activity_Home Activity_Home) {
            this.Activity_Home = Activity_Home;
        }
        @Override
        public void onLocationChanged(Location loc) {
            loc.getLatitude();
            loc.getLongitude();
            LatitudGPS = loc.getLatitude();
            LongitudGPS= loc.getLongitude();
            //Toast.makeText(getApplicationContext(), "Mi ubicacion actual es: Lat = "+ loc.getLatitude() + " Long = " + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            System.out.println("Mi ubicacion actual es: Lat = "+ loc.getLatitude() + " Long = " + loc.getLongitude());
            this.Activity_Home.setLocation(loc);
        }
        @Override
        public void onProviderDisabled(String provider) {
            Log.d("GPS", "GPS DESACTIVADO -- "+ provider);
            contador++;
            if(contador>1){
                mensaje();
                contador=0;
            }
        }
        @Override
        public void onProviderEnabled(String provider) {
            Log.d("GPS", "GPS ACTIVADO -- "+ provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    /****************************************** clima *************************************************/
    public void GetDatosC(){
        QueryC = urlC+LatitudGPS+"&lon="+LongitudGPS+"&appid="+ApiKeyC;
        Log.d("QUERY CLIMA", " -- "+QueryC);
        JsonObjectRequest Respuesta = new JsonObjectRequest(Request.Method.GET, QueryC, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_Objet = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String Temp = String.valueOf(main_Objet.getDouble("temp"));
                    int Humedad =main_Objet.getInt("humidity");
                    String descripcion = object.getString("description");
                    String icon = object.getString("icon");
                    String ciudad = response.getString("name");

                    double centi = ((Double.parseDouble(Temp) - 273.15));
                    int TempC =(int) Math.round(centi);
                    Temperatura.setText(""+TempC);
                    Ciudad.setText(""+ciudad);
                    Descripcion.setText(Descripcion(descripcion));
                    Icono(icon);
                    Centigrados(TempC);

                    AlertaClima(TempC, Humedad);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERROR GETDATOS CLIMA:", ""+error);
            }
        }
        );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(Respuesta);
    }

    public String Descripcion(String descripcion){
        if(descripcion.equals("clear sky")){
            return "Despejado";
        }else if (descripcion.equals("few clouds")){
            return "Pocas Nubes";
        }else if (descripcion.equals("scattered clouds")){
            return "Nubes Dispersas";
        }else if (descripcion.equals("broken clouds") || descripcion.equals("overcast clouds")){
            return "Parcialmente Nublado";
        }else if (descripcion.equals("shower rain") || descripcion.equals("light intensity drizzle") || descripcion.equals("drizzle") || descripcion.equals("heavy intensity drizzle") || descripcion.equals("light intensity drizzle rain") || descripcion.equals("drizzle rain") || descripcion.equals("heavy intensity drizzle rain") || descripcion.equals("shower rain and drizzle") || descripcion.equals("heavy shower rain and drizzle") || descripcion.equals("shower drizzle") || descripcion.equals("light intensity shower rain") || descripcion.equals("heavy intensity shower rain") || descripcion.equals("ragged shower rain")){
            return "Lluvia";
        }else if (descripcion.equals("rain") || descripcion.equals("light rain") || descripcion.equals("moderate rain") || descripcion.equals("heavy intensity rain") || descripcion.equals("very heavy rain") || descripcion.equals("extreme rain")){
            return "Llovizna";
        }else if (descripcion.equals("thunderstorm") || descripcion.equals("thunderstorm with light rain") || descripcion.equals("thunderstorm with rain") || descripcion.equals("thunderstorm with heavy rain") || descripcion.equals("light thunderstorm") || descripcion.equals("heavy thunderstorm") || descripcion.equals("ragged thunderstorm") || descripcion.equals("thunderstorm with light drizzle") || descripcion.equals("thunderstorm with drizzle") || descripcion.equals("thunderstorm with heavy drizzle")){
            return "Tormenta";
        }else if (descripcion.equals("snow") || descripcion.equals("freezing rain") || descripcion.equals("light snow") || descripcion.equals("Heavy snow") || descripcion.equals("Sleet") || descripcion.equals("Light shower sleet") || descripcion.equals("Shower sleet") || descripcion.equals("Light rain and snow") || descripcion.equals("Rain and snow") || descripcion.equals("Light shower snow") || descripcion.equals("Shower snow") || descripcion.equals("Heavy shower snow")){
            return "Nieve";
        }else if (descripcion.equals("mist") || descripcion.equals("Smoke") || descripcion.equals("Haze") || descripcion.equals("fog") || descripcion.equals("sand") || descripcion.equals("dust") || descripcion.equals("volcanic ash") || descripcion.equals("squalls") || descripcion.equals("tornado") || descripcion.equals("sand/ dust whirls") || descripcion.equals("dust whirls")){
            return "Niebla";
        }
        return descripcion;
    }

    public void Icono(String icon){
        if(icon.equals("01d")){
            IconoClima.setImageResource(R.drawable.img_01d);
        }else if (icon.equals("01n")){
            IconoClima.setImageResource(R.drawable.img_01n);
        }else if (icon.equals("02d")){
            IconoClima.setImageResource(R.drawable.img_02d);
        }else if (icon.equals("02n")){
            IconoClima.setImageResource(R.drawable.img_02n);
        }else if (icon.equals("03d") || icon.equals("03n")){
            IconoClima.setImageResource(R.drawable.img_03d);
        }else if (icon.equals("04d") || icon.equals("04n")){
            IconoClima.setImageResource(R.drawable.img_04d);
        }else if (icon.equals("09d") || icon.equals("09n")){
            IconoClima.setImageResource(R.drawable.img_09d);
        }else if (icon.equals("10d")){
            IconoClima.setImageResource(R.drawable.img_10d);
        }else if (icon.equals("10n")){
            IconoClima.setImageResource(R.drawable.img_10n);
        }else if (icon.equals("11d") || icon.equals("11n")){
            IconoClima.setImageResource(R.drawable.img_11d);
        }else if (icon.equals("13d")  || icon.equals("13n")){
            IconoClima.setImageResource(R.drawable.img_13d);
        }else if (icon.equals("50d") || icon.equals("50n")){
            IconoClima.setImageResource(R.drawable.img_50d);
        }
    }

    public void Centigrados(int temperatura){
        if(temperatura<20){
            IconoGrados.setImageResource(R.drawable.climafrio);
        }else if(temperatura>20 && temperatura<30){
            IconoGrados.setImageResource(R.drawable.climacalido);
        }else if(temperatura>30){
            IconoGrados.setImageResource(R.drawable.climacaliente);
        }
    }


    /****************************************** trafico *************************************************/
    public void GetDatosT(){
        QueryT = urlT+LatitudGPS+"%2C"+LongitudGPS+"&unit=KMPH&key="+ApiKeyT;
        Log.d("QUERY TRAFICO", " -- "+QueryT);
        JsonObjectRequest Respuesta = new JsonObjectRequest(Request.Method.GET, QueryT, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_Objet = response.getJSONObject("flowSegmentData");
                    int currentSpeed = main_Objet.getInt("currentSpeed");
                    int freeFlowSpeed = main_Objet.getInt("freeFlowSpeed");
                    double confidence = main_Objet.getDouble("confidence");
                    boolean roadClosure = main_Objet.getBoolean("roadClosure");
                    AlertaTrafico(currentSpeed, freeFlowSpeed, confidence, roadClosure);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERROR GETDATOS TRAFICO:", ""+error);
            }
        }
        );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(Respuesta);

    }

    /****************************************** alertas *************************************************/
    public void mensaje(){
        AlertDialog.Builder Mybuild = new AlertDialog.Builder(this);
        Mybuild.setMessage("NO HAY CONEXION A INTERNET, VERIFIQUE LA CONEXION PARA OBTENER DATOS DEL CLIMA Y TRAFICO");
        Mybuild.setTitle("ERROR !FALLO DE CONEXION A INTERNET!");
        Mybuild.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = Mybuild.create();
        dialog.show();
    }

    private void AlertaClima(int tempC, int humedad) {
        if(tempC<20 && humedad>50){
            notificacion("CLIMA");
            createNotificationChannel("CLIMA");
        }
    }

    private void AlertaTrafico(int currentSpeed, int freeFlowSpeed, double confidence, boolean roadClosure) {
        if(currentSpeed > 50 && freeFlowSpeed > 50 && confidence > 0.4 && roadClosure==false){
            iconoTrafico.setImageResource(R.drawable.semaforo_verde);
        }else if(currentSpeed > 30 && currentSpeed < 50 && freeFlowSpeed > 30 && freeFlowSpeed < 50 && confidence > 0.3 && roadClosure==false){
            iconoTrafico.setImageResource(R.drawable.semaforo_amarillo);
            notificacion("TRAFICO");
            createNotificationChannel("TRAFICO");
        }else if(currentSpeed > 30 && currentSpeed < 50 && freeFlowSpeed > 30 && freeFlowSpeed < 50 && confidence > 0.3 && roadClosure==true){
            iconoTrafico.setImageResource(R.drawable.semaforo_amarillo);
            notificacion("TRAFICO");
            createNotificationChannel("TRAFICO");
        }else if(currentSpeed > 1 && currentSpeed < 30 && freeFlowSpeed > 1 && freeFlowSpeed < 30 && confidence > 0.01 && roadClosure==false){
            iconoTrafico.setImageResource(R.drawable.semaforo_rojo);
        }
    }

    public void notificacion(String problema){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID2)
                .setSmallIcon(R.drawable.advertencia)
                .setContentTitle("ALERTA DE ESTADO")
                .setContentText("EL "+problema+" ESTA CAMBIANDO, TOMA PRECAUCIONES")
                .setVibrate(new long[] {100, 250, 100, 500})
                .setColor(Color.rgb(255,89,89))
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification3))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(CHANNEL_ID, builder.build());
    }

    private void createNotificationChannel(String problema) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ALERTA DE ESTADO";
            String description = "EL "+problema+" ESTA CAMBIANDO, TOMA PRECAUCIONES";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID2, name, importance);
            channel.setDescription(description);
            channel.setVibrationPattern(new long[] {100, 250, 100, 500});
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification3),audioAttributes);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /****************************************** onClicks *************************************************/
    public  void  NewAlarma(View view){
        Intent NewAlarma = new Intent(this, Activity_New_Alarma.class);
        startActivity(NewAlarma);
    }

    public  void  Alarmas(View view){
        Intent  ListarAlarmas = new Intent(Activity_Home.this, ListaAlarmasRecycler.class);
        startActivity(ListarAlarmas);
    }
}