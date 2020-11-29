package com.mfcompany.wakeupontime.utilidades;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 05/11/2020.
 */

public class Utilidades {


    public static final int DB_VERSION = 1;
    public static final SQLiteDatabase.CursorFactory DB_CONTEXT = null;

    public static String TABLA_ALARMA = "alarma";
    public static String TABLA_ID = "id_auto";
    public static String TABLA_ENCABEZADO = "encabezado";
    public static String TABLA_MENSAJE = "mensaje";
    public static String TABLA_FECHA = "fecha";
    public static String TABLA_HORA = "hora";
    public static String TABLA_LATITUD = "latitud";
    public static String TABLA_LONGITUD ="longitud";

    public static final String CREAR_TABLA_ALARMA = "CREATE TABLE "+TABLA_ALARMA+"( "+TABLA_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+TABLA_ENCABEZADO+" TEXT, "+TABLA_MENSAJE+" TEXT, "+TABLA_FECHA+" TEXT, "+TABLA_HORA+" TEXT, "+TABLA_LATITUD+" TEXT, "+TABLA_LONGITUD+" TEXT)";





}
