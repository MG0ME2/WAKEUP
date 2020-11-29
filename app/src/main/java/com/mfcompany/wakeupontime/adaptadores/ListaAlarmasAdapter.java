package com.mfcompany.wakeupontime.adaptadores;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mfcompany.wakeupontime.Activity_New_Alarma;
import com.mfcompany.wakeupontime.entidades.ClassAlarma;
import com.mfcompany.wakeupontime.R;
import java.util.ArrayList;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 25/11/2020.
 */

public class ListaAlarmasAdapter extends RecyclerView.Adapter<ListaAlarmasAdapter.AlarmasViewHolder> implements View.OnClickListener {

    ArrayList<ClassAlarma> listaAlarmas;
    private View.OnClickListener listener;

    public ListaAlarmasAdapter(ArrayList<ClassAlarma> listaAlarmas) {
        this.listaAlarmas = listaAlarmas;
    }

    @NonNull
    @Override
    public AlarmasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_alarmas,null,false);
        view.setOnClickListener(this);
        return new AlarmasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmasViewHolder holder, int position) {
        holder.Encabezado.setText(listaAlarmas.get(position).getEncabezado());
        holder.Mensaje.setText(listaAlarmas.get(position).getMensaje());
        holder.Fecha.setText(listaAlarmas.get(position).getFecha());
        holder.Hora.setText(listaAlarmas.get(position).getHora());
        holder.Ubicacion.setText("Lat: "+listaAlarmas.get(position).getLatitud() +" ; Lon: "+ listaAlarmas.get(position).getLongitud());
    }

    @Override
    public int getItemCount() {
        return listaAlarmas.size();
    }


    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if(listener!=null){
            listener.onClick(v);
        }
    }

    public class AlarmasViewHolder extends RecyclerView.ViewHolder{
        TextView Encabezado, Mensaje, Fecha, Hora, Ubicacion;
        public AlarmasViewHolder(View view) {
            super(view);
            Encabezado = (TextView) itemView.findViewById(R.id.textEncabezado);
            Mensaje = (TextView) itemView.findViewById(R.id.textMensaje);
            Fecha = (TextView) itemView.findViewById(R.id.textFecha);
            Hora = (TextView) itemView.findViewById(R.id.textHora);
            Ubicacion = (TextView) itemView.findViewById(R.id.textUbicacon);
        }
    }
}
