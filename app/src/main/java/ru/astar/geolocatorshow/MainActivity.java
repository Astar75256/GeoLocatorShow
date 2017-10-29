package ru.astar.geolocatorshow;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;


/**
 * Created by Astar on 28.10.2017.
 */

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private LocationManager locationManager;

    private Button myLocationButton;
    private Button whereMyTargetButton;
    private Button addTargetButton;

    private Spinner targetsSpinner;

    private TextView statusText;

    private double latitude;
    private double longitude;

    private MarkerOptions targetMarkerOptions;
    private int userId = 0;
    private String titleMarker;

    private ArrayList<Target> targets;
    private TargetSpinnerAdapter arrayAdapter;

    public static final String SERVER = "http://rrogea75.siteme.org/geo_service.php?";

    public static final String PREF_TARGETS = "targets";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        initUI();

        AppStorage.init(this); // настройки приложения
        targets = new ArrayList<>();
        targets = getTargetsToStorage(); // получаем цели из настроек

        myLocationButton = (Button) findViewById(R.id.myLocation);
        whereMyTargetButton = (Button) findViewById(R.id.whereMyTarget);
        addTargetButton = (Button) findViewById(R.id.addButton);

        targetsSpinner = (Spinner) findViewById(R.id.targetSpinner);

        statusText = (TextView) findViewById(R.id.statusText);

        myLocationButton.setOnClickListener(this);
        whereMyTargetButton.setOnClickListener(this);
        addTargetButton.setOnClickListener(this);

        arrayAdapter = new TargetSpinnerAdapter(MainActivity.this, targets);
        targetsSpinner.setAdapter(arrayAdapter);
        targetsSpinner.setOnItemSelectedListener(itemSelectedListener);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 15F, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 15F, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 15F, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 15F, locationListener);
    }

    private void initUI() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.myLocation:
                showMyLocation();
                break;

            case R.id.whereMyTarget:
                showTarget();
                break;

            case R.id.addButton:
                addTarget();
                break;
        }
    }

    private void addTarget() {
        if (targets != null) {
            final View view = getLayoutInflater().inflate(R.layout.add_dialog_layout, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Добавить цель")
                    .setView(view)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int ii) {
                            // ...
                            EditText nameTarget = (EditText) view.findViewById(R.id.nameEdit);
                            EditText userID = (EditText) view.findViewById(R.id.userIDEdit);

                            String nameText = nameTarget.getText().toString();
                            final int userIDText = Integer.parseInt(userID.getText().toString());

                            if (nameText == null || userIDText <= 0) {
                                dialogInterface.cancel();
                            }

                            targets.add(new Target(nameText, userIDText));
                            saveTargetsToStorage(targets);

                            arrayAdapter.notifyDataSetChanged();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private void saveTargetsToStorage(ArrayList<Target> targets) {
        if (targets != null) {
            Set<String> tempSets = new ArraySet<>();

            if (AppStorage.isContaints(PREF_TARGETS)) {
                Log.d(TAG, "Существует = " + PREF_TARGETS);
                tempSets = AppStorage.getSetProperty(PREF_TARGETS);
            }

            for (Target target:targets) {
                tempSets.add(target.getName() + ":" + target.getUserID());
            }
            AppStorage.addPropertySet(PREF_TARGETS, tempSets);
        }
    }

    private ArrayList<Target> getTargetsToStorage() {
        if (AppStorage.isContaints(PREF_TARGETS)) {
            if (targets == null)
                targets = new ArrayList<>();
            Set<String> tempSets = AppStorage.getSetProperty(PREF_TARGETS);
            for (String s: tempSets) {
                String[] temp = s.split(":");
                Log.d(TAG, "Storage: temp[0] = " + temp[0] + "; temp[1] = " + temp[1]);
                targets.add(new Target(temp[0], Integer.parseInt(temp[1])));
            }
        }
        return targets;
    }

    private void showMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
        map.animateCamera(cameraUpdate);

        StringBuffer status = new StringBuffer();
        status.append(getString(R.string.status) + " Мое местоположение\n");
        status.append("Широта: " + latLng.latitude + "\n");
        status.append("Долгота: " + latLng.longitude + "\n");

        statusText.setText(status.toString());
    }

    private void showTarget() {
        targetMarkerOptions = new MarkerOptions();
        targetMarkerOptions.title(titleMarker);
        targetMarkerOptions.draggable(false);
        new DataTask().execute();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map = gMap;
        map.setMyLocationEnabled(true);
    }

    private class DataTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(SERVER + "action=select&user_id=" + userId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            if (strJson.equals("null")) {
                String message = "Информация недоступна!\n" +
                                 "Возможно указан неверный\n" +
                                 "UserID.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            Log.d("MainActivity", strJson);
            JSONArray dataJsonArray;
            JSONObject dataJsonObject;

            try {
                dataJsonArray = new JSONArray(strJson);
                dataJsonObject = dataJsonArray.getJSONObject(0);

                latitude = dataJsonObject.getDouble("latitude");
                longitude = dataJsonObject.getDouble("longitude");

                LatLng latLng = new LatLng(latitude, longitude);

                targetMarkerOptions.position(latLng);

                map.clear();
                map.addMarker(targetMarkerOptions);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
                map.animateCamera(cameraUpdate);

                StringBuffer sb = new StringBuffer();
                sb.append(getString(R.string.status) + " ");
                sb.append("Широта: " + latitude + "\n");
                sb.append("Долгота: " + longitude + "\n");
                sb.append("User ID: " + dataJsonObject.getString("user_id") + "\n");
                sb.append("Последняя дата: " + dataJsonObject.getString("last_time"));

                statusText.setText(sb.toString());
                Log.d(TAG, sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (view == targetsSpinner) {
                Target target = (Target) arrayAdapter.getItem(pos);
                userId = target.getUserID();
                titleMarker = target.getName();
                Log.d(TAG, "Позиция: " + userId + "; Название: " + titleMarker);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}
