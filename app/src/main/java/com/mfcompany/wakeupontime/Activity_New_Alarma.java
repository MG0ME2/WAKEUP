package com.mfcompany.wakeupontime;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.DatePicker;
import android.widget.Toast;
import java.util.Calendar;

import com.google.android.material.textfield.TextInputEditText;
import com.mfcompany.wakeupontime.entidades.ClassAlarma;
import com.mfcompany.wakeupontime.utilidades.Utilidades;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 10/09/2020.
 */

public class Activity_New_Alarma extends AppCompatActivity {

    ImageButton timeButton, GuardarButton, dateButton, lugarButton;
    TextView hora_alarma, fecha_alarma, textBtnLugar;
    EditText editTextTitulo, editTextMensaje;

    ClassAlarma Alarma;
    String Hora_old, Fecha_old;
    int Estado =0;
    String EstadoActual="";

    ConexionSQLiteHelper conexion;
    SQLiteDatabase db;

    Calendar calendario;
    String fecha_sistema;
    String hora_sistema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__new__alarma);

        timeButton = findViewById(R.id.timeButton);
        dateButton = findViewById(R.id.dateButton);
        lugarButton = findViewById(R.id.lugarButton);
        GuardarButton = findViewById(R.id.GuardarButton);
        hora_alarma = findViewById(R.id.textAlarma);
        fecha_alarma = findViewById(R.id.textAlarmaFecha);
        editTextTitulo = findViewById(R.id.editTextTitulo);
        editTextMensaje = findViewById(R.id.editTextMensaje);
        textBtnLugar = findViewById(R.id.textBtnLugar);

        conexion = new ConexionSQLiteHelper(this);
        db = conexion.getWritableDatabase();

        Bundle Info = getIntent().getExtras();

        if (Info!=null){
            Estado =1;
            EstadoActual="EDITANDO";
            Alarma = (ClassAlarma) Info.getSerializable("alarma");
            Fecha_old = Alarma.getFecha();
            Hora_old = Alarma.getHora();

            editTextTitulo.setText(Alarma.getEncabezado());
            editTextMensaje.setText(Alarma.getMensaje());
            fecha_alarma.setText(Alarma.getFecha());
            hora_alarma.setText(Alarma.getHora());
            lugarButton.setImageResource(R.drawable.eliminar);
            textBtnLugar.setText("ELIMINAR");

            GuardarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(CamposVacios()){
                        Actualizar();
                        Alarmas();
                    }

                }
            });

            lugarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Eliminar(); Alarmas();
                }
            });
        }else{
            Estado =0;
            EstadoActual="CREANDO";
            DateTime();
            hora_alarma.setText(hora_sistema);
            fecha_alarma.setText(fecha_sistema);

            GuardarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (CamposVacios()) {
                        RegistrarAlarma();
                        Alarmas();
                    }
                }
            });

            lugarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IrMapa();
                }
            });
        }

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTimeButton();
            }
        });

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDateButton();
            }
        });
    }

    private void DateTime(){
        calendario = Calendar.getInstance();
        int DAY = calendario.get(Calendar.DAY_OF_MONTH);
        int MONTH = calendario.get(Calendar.MONTH)+1;
        int YEAR = calendario.get(Calendar.YEAR);
        int HOUR = calendario.get(Calendar.HOUR_OF_DAY);
        int MINUTE = calendario.get(Calendar.MINUTE);
        fecha_sistema = DAY+"-"+MONTH+"-"+YEAR;
        hora_sistema = String.format("%02d:%02d", HOUR, MINUTE);
    }

    private boolean CamposVacios(){
        if(editTextTitulo.getText().toString().equals("") && editTextMensaje.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "No Dejes Campos Vacíos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**************************  CREAR ALARMA  **************************************/

    private void RegistrarAlarma() {
        DateTime();
        if(!Validar(fecha_alarma.getText().toString(), hora_alarma.getText().toString())){
            ContentValues Values = new ContentValues();
            Values.put(Utilidades.TABLA_ENCABEZADO, editTextTitulo.getText().toString());
            Values.put(Utilidades.TABLA_MENSAJE, editTextMensaje.getText().toString());
            Values.put(Utilidades.TABLA_FECHA, fecha_alarma.getText().toString());
            Values.put(Utilidades.TABLA_HORA, hora_alarma.getText().toString());
            Values.put(Utilidades.TABLA_LATITUD, "7.8971458");
            Values.put(Utilidades.TABLA_LONGITUD, "-72.5080387");

            try {
                db.insert(Utilidades.TABLA_ALARMA,null, Values);
                Toast.makeText(getApplicationContext(), "REGISTRO EXITOSO", Toast.LENGTH_SHORT).show();
                db.close();
                hora_alarma.setText(hora_sistema);
                fecha_alarma.setText(fecha_sistema);
                editTextTitulo.setText("");
                editTextMensaje.setText("");
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Existe Una Alarma Para La Misma Hora Y Fecha", Toast.LENGTH_LONG).show();
                Log.i("ERROR REGISTRAR ALARMA:", ""+e);
            }
        }
    }

    private boolean Validar(String FECHA, String HORA ){
        DateTime();
        boolean Validacion = false;
        Cursor cursor;
        String[] parametros = {FECHA, HORA};
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

    private  void  IrMapa(){
        Intent IrMapa = new Intent(this, Activity_Mapa.class);
        startActivity(IrMapa);
    }

    private  void  Alarmas(){
        Intent  ListarAlarmas = new Intent(Activity_New_Alarma.this, ListaAlarmasRecycler.class);
        startActivity(ListarAlarmas);
        finish();
   }
    /*********************************************************************************/
    /**************************  EDITAR ALARMA  **************************************/

    private void Actualizar(){

        String[] parametros={Fecha_old,Hora_old};
        ContentValues Values = new ContentValues();
        Values.put(Utilidades.TABLA_ENCABEZADO, editTextTitulo.getText().toString());
        Values.put(Utilidades.TABLA_MENSAJE, editTextMensaje.getText().toString());
        Values.put(Utilidades.TABLA_FECHA, fecha_alarma.getText().toString());
        Values.put(Utilidades.TABLA_HORA, hora_alarma.getText().toString());
        Values.put(Utilidades.TABLA_LATITUD, "7.8971458");
        Values.put(Utilidades.TABLA_LONGITUD, "-72.5080387");
        try {
            db.update(Utilidades.TABLA_ALARMA,Values,Utilidades.TABLA_FECHA+"=? AND "+Utilidades.TABLA_HORA+"=?",parametros);
            Toast.makeText(getApplicationContext(),"ACTUALIZACION EXITOSA",Toast.LENGTH_SHORT).show();
            editTextTitulo.setText("");
            editTextMensaje.setText("");
            db.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"ACTUALIZACION FALLIDA",Toast.LENGTH_SHORT).show();
            Log.i("ERROR ACTUALIZAR:", ""+e);
        }
    }

    private void Eliminar(){
        String[] parametros={Fecha_old,Hora_old};
        db.delete(Utilidades.TABLA_ALARMA, Utilidades.TABLA_FECHA+"=? AND "+Utilidades.TABLA_HORA+"=?",parametros);
        Toast.makeText(getApplicationContext(),"ALARMA ELIMINADA",Toast.LENGTH_SHORT).show();
        db.close();
    }
    /*********************************************************************************/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿NO QUIERES SEGUIR "+EstadoActual+" LA ALARMA?")
                    .setPositiveButton("CONTINUAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(Estado == 0){
                                Intent intent = new Intent(getApplicationContext(), Activity_Home.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Alarmas();
                            }
                        }
                    });
            builder.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void handleTimeButton(){
        Calendar calendar = Calendar.getInstance();
        final int HOUR = calendar.get(Calendar.HOUR_OF_DAY);
        final int MINUTE = calendar.get(Calendar.MINUTE);
        boolean is24FormatoHora = DateFormat.is24HourFormat(this);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String timeString = String.format("%02d:%02d",hour,minute);
                hora_alarma.setText(timeString);
            }
        }, HOUR,MINUTE, is24FormatoHora);
        timePickerDialog.show();
    }

    protected void handleDateButton(){
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DAY = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DATE, day);
                String dateText = DateFormat.format("dd-MM-yyyy", calendar).toString();
                fecha_alarma.setText(dateText);
            }
        }, YEAR, MONTH, DAY);
        datePickerDialog.show();
    }
}