package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;


public class HomeFragment extends Fragment {

    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ListView shopping = (ListView)getView().findViewById(R.id.shopList);
        DocumentReference shopRef = db.collection("privateData/" + mAuth.getUid() + "/Shopping").document("List");

        ListView workout = (ListView)getView().findViewById(R.id.workoutList);
        DocumentReference workoutRef = db.collection("privateData/" + mAuth.getUid() + "/WorkoutPlan").document("List");

        shopping.setNestedScrollingEnabled(true);
        workout.setNestedScrollingEnabled(true);

        showListItems(shopRef, shopping);
        showListItems(workoutRef, workout);
    }
ArrayAdapter adapt;

    private void showListItems(DocumentReference ref, ListView view)
    {
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                if(documentSnapshot.exists()){

                    ArrayList<String> array = new ArrayList<>();

                    Map<String, Object> s = documentSnapshot.getData();
                    for(Map.Entry<String, Object> entry : s.entrySet())
                    {
                        String meal = entry.getKey().toString();
                        String weightcals = entry.getValue().toString();

                        array.add(meal + " " + weightcals);

                    }
                    adapt = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                    view.setAdapter(adapt);

                }
            }
        });
    }
}