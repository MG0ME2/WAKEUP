package com.mfcompany.wakeupontime.entidades;

import java.io.Serializable;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 05/11/2020.
 */

public class ClassAlarma implements Serializable {

    private String Encabezado;
    private String Mensaje;
    private String Fecha;
    private String Hora;
    private String Latitud;
    private String Longitud;

    public ClassAlarma() {
    }

    public ClassAlarma(String encabezado, String mensaje, String fecha, String hora, String latitud, String longitud) {
        Encabezado = encabezado;
        Mensaje = mensaje;
        Fecha = fecha;
        Hora = hora;
        Latitud = latitud;
        Longitud = longitud;
    }

    public String getEncabezado() {
        return Encabezado;
    }

    public void setEncabezado(String encabezado) {
        Encabezado = encabezado;
    }

    public String getMensaje() {
        return Mensaje;
    }

    public void setMensaje(String mensaje) {
        Mensaje = mensaje;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    public String getHora() {
        return Hora;
    }

    public void setHora(String hora) {
        Hora = hora;
    }

    public String getLatitud() {
        return Latitud;
    }

    public void setLatitud(String latitud) {
        Latitud = latitud;
    }

    public String getLongitud() {
        return Longitud;
    }

    public void setLongitud(String longitud) {
        Longitud = longitud;
    }

    @Override
    public String toString() {
        return "ClassAlarma{" +
                "Encabezado='" + Encabezado + '\'' +
                ", Mensaje='" + Mensaje + '\'' +
                ", Fecha='" + Fecha + '\'' +
                ", Hora='" + Hora + '\'' +
                ", Latitud='" + Latitud + '\'' +
                ", Longitud='" + Longitud + '\'' +
                '}';
    }
}
