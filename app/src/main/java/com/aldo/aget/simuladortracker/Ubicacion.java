package com.aldo.aget.simuladortracker;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aldo.aget.simuladortracker.Control.Ext;
import com.aldo.aget.simuladortracker.Service.ServicioTrack;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Ubicacion implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final String LOGTAG = "AGET-Localizacion";

    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int PETICION_CONFIG_UBICACION = 201;

    public GoogleApiClient apiClient;
    Context ctx;
    String telefono;
    Boolean autotrack = false;
    long miliSegundos = 0;
    long miliSegundosTemporales = 0;

    private TextView lblLatitud;
    private TextView lblLongitud;

    private LocationRequest locRequest;

//    public static boolean isNullApi() {
//        if (apiClient != null) {
//            return false;
//        }
//        return true;
//    }
//
//    public static boolean isConnectApi() {
//        boolean resp = false;
//        if (apiClient != null) {
//            if (apiClient.isConnected()) {
//                resp = true;
//            }
//        }
//        return resp;
//    }

    public Ubicacion(Context contexto, String numero, boolean auto, long milSegundos) {
        Log.d(Ext.TAGLOG, "Constructor");
        ctx = contexto;
        telefono = numero;
        autotrack = auto;
        if (!auto) {
            miliSegundosTemporales = milSegundos;
        } else {
            miliSegundos = milSegundos;
        }

        Log.v(LOGTAG, "milisegundos:" + miliSegundos + " milisegundosTemp: " + miliSegundosTemporales);
        //Construcción cliente API Google
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(ctx)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            onStart();
        }
// else if (!isNullApi()) {
//            Log.v(LOGTAG,"La clase no es nula, se iniciara");
//            onStart();
//        }

    }

    protected void onStart() {
        if (!apiClient.isConnected())
            apiClient.connect();

//        if (!autotrack)
//            enableLocationUpdates(miliSegundosTemporales);
//        else
//            enableLocationUpdates(miliSegundos);
//        Toast.makeText(ctx, "Ubicacion iniciada", Toast.LENGTH_SHORT).show();
        enableLocationUpdates();
        Log.v(LOGTAG, "Ubicacion iniciada");
    }

    //    public void enableLocationUpdates(long miliSeg) {
    public void enableLocationUpdates() {

        locRequest = new LocationRequest();
        Log.v(LOGTAG, "milisegundos a actualizar: " + miliSegundos);
        locRequest.setInterval(miliSegundos);
        locRequest.setFastestInterval(miliSegundos + 1000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locSettingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locRequest)
                        .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        apiClient, locSettingsRequest);
        Log.d(LOGTAG, "enableLocationUpdates");

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        Log.d(LOGTAG, "Configuración correcta");
                        startLocationUpdates();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        try {
                        Log.d(LOGTAG, "Se requiere actuación del usuario");
//                            status.startResolutionForResult(new Activity(), PETICION_CONFIG_UBICACION);
//                            status.startResolutionForResult(Ubicacion.this, PETICION_CONFIG_UBICACION);

                        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        long[] pattern = new long[]{1000, 500, 1000};
                        NotificationManager nm = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
                        ;
                        final int ID_NOTIFICACION_CREAR = 1;
                        Notification.Builder builder = new Notification.Builder(ctx);
                        builder.setAutoCancel(false);
                        builder.setTicker("Por favor encienda el GPS");
                        builder.setContentTitle("Encienda el GPS");
                        builder.setContentText("El uso de este servicio hace uso del GPS");
                        builder.setSmallIcon(R.drawable.ic_location_on_blue);
                        builder.setAutoCancel(true);
                        builder.setSubText("Abra la aplicacion despues de encender el GPS");
                        builder.setSound(defaultSound);        // Uso en API 11 o mayor
                        builder.setVibrate(pattern);
                        builder.setOnlyAlertOnce(true);

                        nm.notify(ID_NOTIFICACION_CREAR, builder.build());

//                        } catch (IntentSender.SendIntentException e) {
                        Log.i(LOGTAG, "Error al intentar solucionar configuración de ubicación");
//                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(LOGTAG, "No se puede cumplir la configuración de ubicación necesaria");

                        break;
                }
            }
        });
    }


    public void disableLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(
                apiClient, this);
        apiClient.disconnect();
    }


    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (apiClient.isConnected()) {

                Log.d(LOGTAG, "Inicio de recepción de ubicaciones");
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        apiClient, locRequest, this);
            } else {
                Log.d(LOGTAG, "No se ha conectado el api se intentara reconectar... ");
                apiClient.connect();
//                Log.d(LOGTAG, "Segundo intento");
//                if (apiClient.isConnected()) {
//                    Log.d(LOGTAG, "Inicio de recepción de ubicaciones");
//                    LocationServices.FusedLocationApi.requestLocationUpdates(
//                            apiClient, locRequest, this);
//                } else {
//                    Log.d(LOGTAG, "No se ha conectado el api");
//                }
            }
        } else {
            Log.d(LOGTAG, "No estan los permisos establecidos ponga en el manifieto los permisos: ACCESS_FINE_LOCATION");
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //Se ha producido un error que no se puede resolver automáticamente
        //y la conexión con los Google Play Services no se ha establecido.

        Log.e(LOGTAG, "Error grave al conectar con Google Play Services");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Conectado correctamente a Google Play Services

        if (ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(ctx,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    PETICION_PERMISO_LOCALIZACION);
        } else {
            Location lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(apiClient);
//            updateUI(lastLocation);
            Toast.makeText(ctx, "Se enviarian las coordenadas almacenadas, no actualizadas", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Se ha interrumpido la conexión con Google Play Services
        Log.e(LOGTAG, "Se ha interrumpido la conexión con Google Play Services");
    }

    private void updateUI(Location loc) {
        if (loc != null) {

            String latitud = String.valueOf(loc.getLatitude());
            String longitud = String.valueOf(loc.getLongitude());
            float speed = loc.getSpeed();
            SmsManager sms = SmsManager.getDefault();
            String msn = "lat:" + latitud + "\nlon:" + longitud + "\nspeed:"+speed + "\nbat:"+cargaBateria()+"%" + "\nhttp://maps.google.com/maps?f=q&q=" + latitud + "," + longitud;
            sms.sendTextMessage(telefono, null, msn, null, null);
            Log.v(LOGTAG, msn);

//            Log.v(LOGTAG,"latitud: "+latitud);
//            Log.v(LOGTAG,"longitud: "+longitud);

        } else {
            Toast.makeText(ctx, "Latitud: (desconocida)", Toast.LENGTH_SHORT).show();
            Toast.makeText(ctx, "Longitud: (desconocida)", Toast.LENGTH_SHORT).show();
        }
        if (!autotrack) {
            disableLocationUpdates();
        }
//        else if (!autotrack && ServicioTrack.isInstanceCreated()) {
//            enableLocationUpdates(miliSegundos);
//        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOGTAG, "Recibida nueva ubicación!");
        //Mostramos la nueva ubicación recibida
        updateUI(location);
    }

    public int cargaBateria() {
        try {
            IntentFilter batIntentFilter =
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent battery = ctx.registerReceiver(null, batIntentFilter);
            int nivelBateria = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            return nivelBateria;
        } catch (Exception e) {
            Toast.makeText(ctx, "Error al obtener estado de la batería", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }
}