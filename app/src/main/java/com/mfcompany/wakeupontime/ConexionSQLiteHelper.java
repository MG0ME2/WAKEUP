package com.mfcompany.wakeupontime;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mfcompany.wakeupontime.utilidades.Utilidades;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 05/11/2020.
 */

public class ConexionSQLiteHelper extends SQLiteOpenHelper {

    public ConexionSQLiteHelper(Context context) {
        super(context, Utilidades.TABLA_ALARMA, Utilidades.DB_CONTEXT, Utilidades.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Utilidades.CREAR_TABLA_ALARMA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS alarma");
        onCreate(db);
    }
}
