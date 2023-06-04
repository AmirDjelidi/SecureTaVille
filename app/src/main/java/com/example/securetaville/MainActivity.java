package com.example.securetaville;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.securetaville.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button addMarkerButton;
    private Button doneButton;
    private Button clearButton;
    private Circle circle;
    private Marker invisibleMarker;
    private String circleAddress;
    ArrayList<String> Adresse;
    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        addMarkerButton = findViewById(R.id.fab);
        doneButton = findViewById(R.id.done);
        clearButton = findViewById(R.id.clear);
        Adresse = new ArrayList<>();

        sharedPreferences = getSharedPreferences("location", 0);


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_addresses) {
                // Ouvrir AddressListActivity
                Intent addressIntent = new Intent(MainActivity.this, AddressListActivity.class);
                addressIntent.putStringArrayListExtra("adresses", Adresse);
                startActivity(addressIntent);
                return true;
            }
            // Gérer d'autres éléments du menu si nécessaire
            return false;
        });

        addMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = googleMap.getCameraPosition().target;
                if (circle != null) {
                    circle.remove();
                }
                CircleOptions circleOptions = new CircleOptions()
                        .center(latLng)
                        .radius(30)
                        .strokeColor(Color.rgb(255, 108, 0))
                        .fillColor(Color.argb(120, 255, 156, 0))
                        .clickable(true);
                circle = googleMap.addCircle(circleOptions);
                doneButton.setVisibility(View.VISIBLE);
                clearButton.setVisibility(View.VISIBLE);
                addMarkerButton.setVisibility(View.INVISIBLE);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circle != null) {
                    circle.setFillColor(Color.argb(120, 255, 156, 0));
                    Toast.makeText(MainActivity.this, "Vous avez délimité une zone", Toast.LENGTH_SHORT).show();

                    // Créer un marker invisible sur le centre du cercle
                    invisibleMarker = googleMap.addMarker(new MarkerOptions()
                            .position(circle.getCenter())
                            .alpha(0)
                            .snippet("Ajouté à " + getCurrentTime()));

                    // Récupérer l'adresse correspondante à la position du marker invisible
                    if (invisibleMarker != null) {
                        LatLng latLng = invisibleMarker.getPosition();
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);
                                circleAddress = address.getAddressLine(0);
                                invisibleMarker.setTitle(circleAddress); // Ajouter l'adresse comme titre du marker invisible
                                invisibleMarker.showInfoWindow();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("adresses : ", circleAddress.toString());
                                editor.commit();


                                // Afficher la bulle d'info du marker invisible
                                // Utilisez la variable circleAddress comme vous le souhaitez
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                circle = null;
                doneButton.setVisibility(View.INVISIBLE);
                clearButton.setVisibility(View.INVISIBLE);
                addMarkerButton.setVisibility(View.VISIBLE);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circle != null) {
                    circle.remove();
                    circle = null;
                    // Autres actions à effectuer lors de la suppression du cercle
                }
                doneButton.setVisibility(View.INVISIBLE);
                clearButton.setVisibility(View.INVISIBLE);
                addMarkerButton.setVisibility(View.VISIBLE);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(new Date());
    }
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        getCurrentLocation();
    }

    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mapFragment.getMapAsync(MainActivity.this);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.custom_map_style));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setMaxZoomPreference(16);
        googleMap.setMinZoomPreference(12);



        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (circle != null) {
                    circle.setCenter(googleMap.getCameraPosition().target);
                }
            }
        });
        googleMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                if (circle.getTag() != null && circle.getTag().equals("circle")) {
                    // Clic sur le cercle invisible
                    LatLng latLng = circle.getCenter();
                    getMarkerAddress(latLng);
                }
            }
        });
        // Centrer la caméra sur la localisation actuelle
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                }
            }
        });

        addCircleAndMarkerIfAddressesExist();
    }
    private void getMarkerAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String circleAddress = address.getAddressLine(0);
                Adresse.add(circleAddress);
                saveAddresses(Adresse);
                Toast toast = Toast.makeText(MainActivity.this, "Adresse : " + circleAddress, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAddresses(List<String> addresses) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("addresses", new HashSet<>(addresses));
        editor.apply();
    }

    private void addCircleAndMarkerIfAddressesExist() {
        if (!Adresse.isEmpty()) {
            String lastAddress = Adresse.get(Adresse.size() - 1); // Récupérer la dernière adresse ajoutée
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(lastAddress, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    // Ajouter le cercle
                    if (circle != null) {
                        circle.remove();
                    }
                    CircleOptions circleOptions = new CircleOptions()
                            .center(latLng)
                            .radius(30)
                            .strokeColor(Color.rgb(255, 108, 0))
                            .fillColor(Color.argb(120, 255, 156, 0))
                            .clickable(true);
                    circle = googleMap.addCircle(circleOptions);

                    // Ajouter le marqueur
                    invisibleMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .alpha(0)
                            .snippet("Ajouté à " + getCurrentTime()));

                    // Afficher la bulle d'info du marqueur invisible
                    if (invisibleMarker != null) {
                        invisibleMarker.setTitle(lastAddress);
                        invisibleMarker.showInfoWindow();
                    }

                    // Centrer la caméra sur le marqueur
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

                    Toast.makeText(this, "Adresse ajoutée : " + lastAddress, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}