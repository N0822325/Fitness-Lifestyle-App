package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

public class ProfileFragment extends Fragment {

    View view;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }


    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView workout = (TextView)getView().findViewById(R.id.WorkoutNumber);
        TextView meal = (TextView)getView().findViewById(R.id.MealNumber);

        setText(workout,"/Workouts");
        setText(meal,"/Meals");

    }


    private void setText(TextView text, String path)
    {


        db.collection("publicData/" + mAuth.getUid() + path)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        int Count = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Count++;
                        }
                        text.setText(Integer.toString(Count));


                    }
                });

    }

}
