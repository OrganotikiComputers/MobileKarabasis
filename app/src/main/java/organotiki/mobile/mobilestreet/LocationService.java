package organotiki.mobile.mobilestreet;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.GpsCoordinates;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

public class LocationService extends Service implements Communicator {

    Realm realm;
    VolleyRequests request;
    GlobalVar gVar;
    private Handler handler;
    private Runnable runnable;
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private PowerManager.WakeLock wakeLock;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        try{
            realm = Realm.getDefaultInstance();
            request=new VolleyRequests();
            gVar = realm.where(GlobalVar.class).findFirst();

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "LocationService::WakeLock");
            wakeLock.acquire();

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        GpsCoordinates newCoordinates = new GpsCoordinates(UUID.randomUUID().toString(), gVar.getMyUser().getUsername(),currentDate,currentTime,String.valueOf(latitude),String.valueOf(longitude));
                        realm.beginTransaction();
                        GpsCoordinates coordinates =realm.copyToRealmOrUpdate(newCoordinates);
                        realm.commitTransaction();
                        // Handle the location update here
                        request=new VolleyRequests();
                        request.SendCoordinates(LocationService.this);
                    }
                }
            };
            createNotificationChannel();
            handler = new Handler(Looper.getMainLooper());
            runnable = new Runnable() {
                @Override
                public void run() {
                    requestLocationUpdates();
                    handler.postDelayed(this, 20000);
                }
            };
            handler.post(runnable);
        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }

    }

    /*private Notification createNotification() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Running in the background")
                .setSmallIcon(R.drawable.logoo_icon)
                .setContentIntent(pendingIntent)
                .build();
    }*/

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void requestLocationUpdates() {
        try{
          /*  LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(20000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);*/
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 20000)
                    .setMinUpdateIntervalMillis(10000)
                    .build();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mobile Street")
                .setContentText("GPS Service is running...")
                .setSmallIcon(R.drawable.logo)
                .build();
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //handler.removeCallbacks(runnable);
        //fusedLocationClient.removeLocationUpdates(locationCallback);
       /* realm.close();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }*/
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void respondCustomerSearch(Customer customer, Address address) {

    }

    @Override
    public void respondInvoiceType() {

    }

    @Override
    public void respondPaymentTerm() {

    }

    @Override
    public void respondCustomerCreate() {

    }

    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        switch (position) {
            case 0:
                try {
                    String messageFail = jsonObject.getString("Message");
                    Log.d("asdfgF", messageFail);

                    //Toast.makeText(this, message,Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 1:
                try {
                    String messageSuccess = jsonObject.getString("Message");
                    Log.d("asdfgS", messageSuccess);

                    //Toast.makeText(this, message,Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 2:
                try {
                    String messageNoCoordinates = jsonObject.getString("Message");
                    Log.d("asdfgN", messageNoCoordinates);

                    //Toast.makeText(this, message,Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
        }
    }

    @Override
    public void respondDate(Integer position, int year, int month, int day) {

    }

    @Override
    public void respondCompanySite() {

    }

    @Override
    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> sLines) {

    }
}