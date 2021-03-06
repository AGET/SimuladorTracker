package com.aldo.aget.simuladortracker.Control;

/**
 * Created by Work on 25/11/16.
 */


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by workstation on 10/11/15.
 */
public class ManagerDB {

    SQLHelper sqlhelper;
    SQLiteDatabase db;

    public ManagerDB(Context contexto) {
        sqlhelper = new SQLHelper(contexto);
    }

    public int numberUser(String tabla, String[] columna) {
        abrirEscrituraBD();
        Cursor c;
        c = db.query(tabla, columna, null, null, null, null, null);

        Log.d(Ext.TAGLOG, "getColumnCount=" + c.getColumnCount());
        Log.d(Ext.TAGLOG, "getCount=" + c.getCount());

        return c.getCount();
    }


    public boolean existe(String numero) {
        abrirEscrituraBD();

        Cursor c;
        c = db.query(SQLHelper.TABLA_USUARIOS, new String[]{SQLHelper.COLUMNA_USUARIO_NUMERO},
                SQLHelper.COLUMNA_USUARIO_NUMERO + "=?", new String[]{numero}, null, null, null);


        Log.d(Ext.TAGLOG, "getColumnCount=" + c.getColumnCount());
        Log.d(Ext.TAGLOG, "getCount=" + c.getCount());
        Log.d(Ext.TAGLOG, "numero=" + numero);

        return c.getCount() > 0;
    }

    public boolean autotrakEstablecido() {
        abrirEscrituraBD();
        Cursor c;
        c = db.query(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO}, null, null, null, null, null);

        Log.d(Ext.TAGLOG, "Autotrack getColumnCount=" + c.getColumnCount());
        Log.d(Ext.TAGLOG, "Autotrack getCount=" + c.getCount());

        return c.getCount() > 0;
    }

    public boolean eliminarItem(String tabla, String columna, String dato) {
        abrirEscrituraBD();
        db.delete(tabla, columna + "=" + dato, null);
        return true;
    }

    public int eliminarTodo(String tabla) {
        abrirEscrituraBD();
        int codigo = db.delete(tabla, null, null);
        Log.v("DELIMINADO ","DB CODE: "+codigo);
        cerrarBD();
        return codigo;
    }

    public void abrirEscrituraBD() {
        db = sqlhelper.getWritableDatabase();
    }

    public void cerrarBD() {
        db.close();
    }


    public boolean actualizarUnDato(String tabla, String columna, String dato, String condicion, String valorCondicion) {
        abrirEscrituraBD();
        ContentValues valores = new ContentValues();
        valores.put(columna, dato);
        db.update(tabla, valores, condicion + "=" + valorCondicion, null);
        return true;
    }

    public ArrayList obtenerDatos(String tabla, String[] campos, String where, String[] datosWhere) {
        abrirEscrituraBD();
        ArrayList datosCursor = new ArrayList();
        Cursor c;
        try {
            if (where != null) {
                c = db.query(tabla, campos, where + "=?", datosWhere, null, null, null);
            } else {
                c = db.query(tabla, campos, null, null, null, null, null);
            }
        } catch (SQLiteException e) {
            Log.v(Ext.TAGLOG, "Error SQLite" + e.getMessage());
            return null;
        }
        if (c.moveToFirst()) {
            do {
//                Log.v(Ext.TAGLOG, "numero: " + c.getColumnCount());
//                Log.v(Ext.TAGLOG, "otro numero: " + c.getCount());
//                Log.v(Ext.TAGLOG, "numero de nombres: " + c.getColumnNames().length);
//                Log.v(Ext.TAGLOG, "nombre1: " + c.getColumnNames()[0]);
//                Log.v(Ext.TAGLOG, "nombre2: " + c.getColumnNames()[1]);
                for (int i = 0; i < c.getColumnCount(); i++) {
                    datosCursor.add(c.getString(i));
//                    Log.v(Ext.TAGLOG, c.getColumnName(i) + ": " + c.getString(i));
                }
            } while (c.moveToNext());
        } else {
            datosCursor = null;
        }
        return datosCursor;
    }


    public void insertar(String tabla, String columna, String datosColumnas) {
        abrirEscrituraBD();
        ContentValues nuevoRegistro = new ContentValues();
        switch (tabla) {
            case SQLHelper.TABLA_USUARIOS:
                nuevoRegistro.put(columna, datosColumnas);
                break;
            case SQLHelper.TABLA_AUTOTRACK:
                nuevoRegistro.put(columna, datosColumnas);
                break;
        }
        db.insert(tabla, null, nuevoRegistro);
        cerrarBD();
    }

    public long insercionMultiple(String tabla, String columna[], String datosColumnas[]) {
        abrirEscrituraBD();
        ContentValues nuevoRegistro = new ContentValues();
        for (int i = 0; i < columna.length; i++) {
            nuevoRegistro.put(columna[i], datosColumnas[i]);
        }
        long respuesta = db.insert(tabla, null, nuevoRegistro);
        cerrarBD();
        return respuesta;
    }
}