package com.mfcompany.wakeupontime;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mfcompany.wakeupontime.adaptadores.ListaAlarmasAdapter;
import com.mfcompany.wakeupontime.entidades.ClassAlarma;
import com.mfcompany.wakeupontime.utilidades.Utilidades;
import java.util.ArrayList;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 25/09/2020.
 */

public class ListaAlarmasRecycler extends AppCompatActivity {

    ArrayList<ClassAlarma> listaAlarma;
    RecyclerView recyclerViewAlarmas;
    ConexionSQLiteHelper conexion;
    ClassAlarma Alarma;
    private String Encabezado, Mensaje, Fecha, Hora, Latitud, Longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alarmas_recycle);

        conexion = new ConexionSQLiteHelper(this);

        listaAlarma = new ArrayList<>();

        recyclerViewAlarmas = (RecyclerView) findViewById(R.id.recyclerAlarmas);
        recyclerViewAlarmas.setLayoutManager(new LinearLayoutManager(this));

        consultarListaAlarmas();

        ListaAlarmasAdapter adapter = new ListaAlarmasAdapter(listaAlarma);
        adapter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Encabezado = listaAlarma.get(recyclerViewAlarmas.getChildAdapterPosition(view)).getEncabezado();
                Mensaje = listaAlarma.get(recyclerViewAlarmas.getChildAdapterPosition(view)).getMensaje();
                Fecha = listaAlarma.get(recyclerViewAlarmas.getChildAdapterPosition(view)).getFecha();
                Hora = listaAlarma.get(recyclerViewAlarmas.getChildAdapterPosition(view)).getHora();
                Latitud = listaAlarma.get(recyclerViewAlarmas.getChildAdapterPosition(view)).getLatitud();
                Longitud = listaAlarma.get(recyclerViewAlarmas.getChildAdapterPosition(view)).getLongitud();

                Alarma = new ClassAlarma(Encabezado, Mensaje, Fecha, Hora, Latitud, Longitud);
                System.out.println(Alarma.toString());

                Intent intent = new Intent(getApplicationContext(), Activity_New_Alarma.class);
                Bundle Info = new Bundle();
                Info.putSerializable("alarma",Alarma);
                intent.putExtras(Info);
                startActivity(intent);
                finish();
            }
        });
        recyclerViewAlarmas.setAdapter(adapter);
    }

    private void consultarListaAlarmas() {
        SQLiteDatabase db = conexion.getReadableDatabase();
        ClassAlarma Alarma = null;
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM "+ Utilidades.TABLA_ALARMA, null);
            while (cursor.moveToNext()){
                Alarma = new ClassAlarma();
                Alarma.setEncabezado(cursor.getString(1));
                Alarma.setMensaje(cursor.getString(2));
                Alarma.setFecha(cursor.getString(3));
                Alarma.setHora(cursor.getString(4));
                Alarma.setLatitud(cursor.getString(5));
                Alarma.setLongitud(cursor.getString(6));
                listaAlarma.add(Alarma);
            }
            cursor.close();
        }catch (Exception e){
            Log.i("ERROR CONSULTAR ALARMAS:", ""+e);
        }
    }
}
