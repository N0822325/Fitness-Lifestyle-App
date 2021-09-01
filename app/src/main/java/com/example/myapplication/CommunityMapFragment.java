package com.example.myapplication;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommunityMapFragment extends Fragment implements OnMapReadyCallback {

    View view;

    GoogleMap mMap;
    List<LatLng> pointsList = new ArrayList<LatLng>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private CollectionReference runRef;

    ListView userListView;
    ArrayAdapter userAdapter;
    View userSelected;
    Object userParent;

    ListView runListView;
    ArrayAdapter runAdapter;
    View runSelected;
    Object runParent;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_map_plan, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        runRef = db.document("publicData/" + mAuth.getUid()).collection("Running");





    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Log.d("ggggggggg","document.getId() + + document.getData()");
        mMap = googleMap;


        userListView = (ListView)getView().findViewById(R.id.UserList);
        runListView = (ListView)getView().findViewById(R.id.RunList);

        userListView.setNestedScrollingEnabled(true);
        runListView.setNestedScrollingEnabled(true);

        // Read Head List
        readHead();

        // Select Head List Item
        selectHead();

        // Select Group List Item
        selectGroupItems();

    }


    private void readHead()
    {
        db.collection("publicData/")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    ArrayList<String> array = new ArrayList<>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Map<String,Object> data = document.getData();
                                for (Map.Entry<String, Object> pair : data.entrySet()) {
                                    array.add(pair.getValue().toString());
                                }

                                Log.d("TAG",document.getId() + " => " + document.getData());
//                                Toast.makeText(getActivity(),document.getId(),Toast.LENGTH_SHORT).show();
                            }
                            userAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                            userListView.setAdapter(userAdapter);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void selectHead()
    {
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean notFirst = true;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!notFirst) { userSelected.setBackgroundColor(Color.WHITE); }

                int scrollY = userListView.getFirstVisiblePosition();

                userParent = parent.getItemAtPosition(position);
                String selectedItem = (String) userParent;

                userSelected = parent.getChildAt(position-scrollY);
                userSelected.setBackgroundColor(getResources().getColor(R.color.purple_200));

                db.collection("publicData/")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                runAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, new ArrayList());
                                runListView.setAdapter(runAdapter);

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    Map<String, Object> data = document.getData();
                                    for (Map.Entry<String, Object> pair : data.entrySet()) {
                                        if(pair.getValue().toString().equals(selectedItem))
                                        {
                                            userRef = db.collection("publicData/").document(document.getId());
                                            showListItems();
                                            Log.d("Loggert_Froggert", document.getId());
                                        }
                                    }
                                }



                            }
                        });



                notFirst = false;
            }
        });
    }


    private void showListItems()
    {
        ArrayList<String> array = new ArrayList<>();

        userRef.collection("Running").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                array.add(document.getId());

                                Log.d("TAG",document.getId() + " => " + document.getData());
//                                Toast.makeText(getActivity(),document.getId(),Toast.LENGTH_SHORT).show();
                            }

                            runAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                            runListView.setAdapter(runAdapter);

                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }


                    }
                });
    }

    private void selectGroupItems()
    {
//        runListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            boolean notFirst = true;

        runListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean notFirst = true;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ggggggggg", "document.getId()");
                if (!notFirst) { runSelected.setBackgroundColor(Color.WHITE); }

                int scrollY = runListView.getFirstVisiblePosition();

                runParent = parent.getItemAtPosition(position);
                String selectedItem = (String) runParent;

                runSelected = parent.getChildAt(position-scrollY);
                runSelected.setBackgroundColor(getResources().getColor(R.color.purple_200));

                userRef.collection("Running").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    if (document.getId().equals(selectedItem)) {
                                        Map<String,Object> a  = document.getData();

                                        int index = 2;
                                        String[] parts = a.values().toString().split("\\}|\\{|=|, ");

                                        try {
                                            while(true){
                                                pointsList.add(new LatLng(
                                                        Double.parseDouble(parts[index]),
                                                        Double.parseDouble(parts[index+2])));
                                                index+=6;
                                                Log.d("ggggggggg", parts[index] + parts[index+2]);
                                            }
                                        } catch (Exception e) {}

                                    }
                                }

                                LatLng previous = pointsList.get(0);

                                // Clears the previously touched position
                                mMap.clear();

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(previous);
                                markerOptions.title("Start");
                                mMap.addMarker(markerOptions);

                                // Animating to the touched position
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(previous)
                                        .zoom(17).build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                for(LatLng current : pointsList)
                                {
                                    Log.d("ggggggggg", current.toString());
                                    if (current == previous) { continue; }

                                    Polyline line = mMap.addPolyline(new PolylineOptions()
                                            .add(previous, current)
                                            .width(5)
                                            .color(Color.RED));

                                    previous = current;
                                }


                                notFirst = false;
                            }
                        });


            }
        });
    }

}
