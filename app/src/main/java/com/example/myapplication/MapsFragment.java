package com.example.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    View view;
    private GoogleMap mMap;
    private LocationTrack locationTrack;

    Location Start;
    boolean start = true;
    LatLng startPoint;
    LatLng nextPoint;
    List<LatLng> pointsList = new ArrayList<LatLng>();

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference cRef;

    String RunName;

    private final static int ALL_PERMISSIONS_RESULT = 101;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_maps, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cRef = db.document("publicData/" + mAuth.getUid()).collection("Running");

        locationTrack = new LocationTrack(getActivity());

        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        if (locationTrack.canGetLocation()) {


            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

            Toast.makeText(getContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {

            locationTrack.showSettingsAlert();
        }

        Button FinishRecording = (Button)getView().findViewById(R.id.FinishButton);
        FinishRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String,Object> data = new HashMap<>();
                data.put("data",pointsList);
                RunName = Calendar.getInstance().getTime().toString();

                cRef.document(RunName).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(),"Group Added",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. ssThis method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Map Click Thread
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
//                MarkerOptions markerOptions = new MarkerOptions();
//
//                // Setting the position for the marker
//                markerOptions.position(latLng);
//
//                // Setting the title for the marker.
//                // This will be displayed on taping the marker
//                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
//
//                // Clears the previously touched position
//                //mMap.clear();
//
//                // Animating to the touched position
//                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//
//                // Placing a marker on the touched position
//                mMap.addMarker(markerOptions);
            }
        });


        // Location Change
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());



        executor.execute(new Runnable() {
            @Override
            public void run() {



                while (true) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            while(start == true)
                            {
                                if (locationTrack.locationChanged) {
                                    Double lat = locationTrack.getLatitude();
                                    Double lng = locationTrack.getLongitude();

                                    startPoint = new LatLng(lat, lng);
                                    nextPoint = startPoint;

                                    updateLocation(lat,lng);

//                                    pointsList.add(startPoint);
//                                    pointsList.add(startPoint);

                                    start = false;
                                    return;
                                }
                            }


//                            Log.d("Logg", "hdadsdaa");


                            if (locationTrack.locationChanged) {
                                Double lat = locationTrack.getLatitude();
                                Double lng = locationTrack.getLongitude();

                                if((lat != nextPoint.latitude) && (lng != nextPoint.longitude))
                                {
                                    Log.d("Logg", Double.toString(lat));
                                    Log.d("Logg", Double.toString(nextPoint.latitude));
                                    updateLocation(lat, lng);
                                    locationTrack.locationChanged = false;
                                }



                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });


    }



    private void updateLocation(Double lat, Double lng)
    {


        MarkerOptions markerOptions = new MarkerOptions();

        // Creating a marker


        nextPoint = new LatLng(lat, lng);


        // Setting the position for the marker
        markerOptions.position(nextPoint);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(nextPoint.latitude + " : " + nextPoint.longitude);

        // Clears the previously touched position
        mMap.clear();

        // Animating to the touched position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(nextPoint)
                .zoom(17).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Placing a marker on the touched position
        mMap.addMarker(markerOptions);

        pointsList.add(nextPoint);
        LatLng previous = startPoint;

        for(LatLng current : pointsList)
        {
            if (current == previous) { continue; }

            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(previous, current)
                    .width(5)
                    .color(Color.RED));

            previous = current;
        }

//
    }



    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}