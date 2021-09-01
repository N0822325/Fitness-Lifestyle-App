package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class WorkoutPlanFragment extends Fragment {

    View view;

    ListView headListView;
    ArrayAdapter headAdapter;
    View headSelected;
    Object headParent;

    ListView workoutListView;
    ArrayAdapter workoutAdapter;
    View workoutSelected;
    Object workoutParent;

    private FirebaseAuth mAuth;
    private boolean isGuest;
    private FirebaseFirestore db;
    private DocumentReference dbRef;
    private CollectionReference cRef;
    private DocumentReference dbPlanRef;
    String currentCollection;

    ArrayList<String> PlanningList = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_workout_plan, container, false);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        isGuest = mAuth.getInstance().getCurrentUser() == null;
        if(!isGuest)
        {
            db = FirebaseFirestore.getInstance();
            cRef = db.document("publicData/" + mAuth.getUid()).collection("Workouts");
            dbPlanRef = db.collection("privateData/" + mAuth.getUid() + "/WorkoutPlan").document("List");
        }

        headListView = (ListView)getView().findViewById(R.id.UserList);
        workoutListView = (ListView)getView().findViewById(R.id.GroupList);

        headListView.setNestedScrollingEnabled(true);
        workoutListView.setNestedScrollingEnabled(true);

        // Read Head List
        readHead();

        // Select Head List Item
        selectHead();

        // Add Head
        final Button addHBtn = (Button)getView().findViewById(R.id.addGroup);
        addHBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToList();
            }
        });

//---------------------------------------//

        // Read Plan List
        showListItems();

        // Select Plan List Item
        selectListItems();

        // Delete Element in Plan List
        Button deleteWBtn = (Button)getView().findViewById(R.id.deleteWorkout);
        deleteWBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemFromList();
            }
        });



    }


    private void readHead()
    {
        db.collection("publicData/" + mAuth.getUid() + "/Workouts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    ArrayList<String> array = new ArrayList<>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                array.add(document.getId());

                                Log.d("TAG",document.getId() + " => " + document.getData());
//                                Toast.makeText(getActivity(),document.getId(),Toast.LENGTH_SHORT).show();
                            }
                            headAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                            headListView.setAdapter(headAdapter);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void selectHead()
    {
        headListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean notFirst = true;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!notFirst) { headSelected.setBackgroundColor(Color.WHITE); }

                int scrollY = headListView.getFirstVisiblePosition();

                headParent = parent.getItemAtPosition(position);
                String selectedItem = (String) headParent;

                headSelected = parent.getChildAt(position-scrollY);
                headSelected.setBackgroundColor(getResources().getColor(R.color.purple_200));

                dbRef = db.collection("publicData/" + mAuth.getUid()  + "/Workouts").document(selectedItem);
                Log.d("Loggert_Froggert", selectedItem);

                notFirst = false;
            }
        });
    }



    private void showListItems()
    {
        dbPlanRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                if(documentSnapshot.exists()){

                    ArrayList<String> array = new ArrayList<>();

                    Map<String, Object> s = documentSnapshot.getData();
                    for(Map.Entry<String, Object> entry : s.entrySet())
                    {
                        Log.d("Loggert_Froggert", "eee");
                        String workout = entry.getKey().toString();
                        String weightcals = entry.getValue().toString();

                        PlanningList.add(0,workout);
                        array.add(workout + " " + weightcals);

                    }

                    workoutAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                    workoutListView.setAdapter(workoutAdapter);

                }
            }
        });
    }


    private void selectListItems()
    {
        workoutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean notFirst = true;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!notFirst) { workoutSelected.setBackgroundColor(Color.WHITE); }

                int scrollY = workoutListView.getFirstVisiblePosition();

                workoutParent = parent.getItemAtPosition(position);
                String selectedItem = (String) workoutParent;

                workoutSelected = parent.getChildAt(position-scrollY);
                workoutSelected.setBackgroundColor(getResources().getColor(R.color.purple_200));

                notFirst = false;
            }
        });
    }

    private void addItemToList()
    {

        if(dbRef == null) { return; }
        dbRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                if(documentSnapshot.exists()){

                    ArrayList<String> array = new ArrayList<>();

                    Map<String, Object> s = documentSnapshot.getData();
                    for(Map.Entry<String, Object> entry : s.entrySet())
                    {


                        HashMap<String, Object> output = new HashMap<>();
                        String name = entry.getKey();
                        int Count = 2;

                        for(String existing : PlanningList)
                        {
                            if(existing.contains(name))
                            {
                                Log.d("Loggert_Froggert",name);
                                Log.d("Loggert_Froggert",existing);
                                name = entry.getKey();
                                name = name + " x" + Count;
                                Count += 1;
                            }
                        }
                        output.put(name, entry.getValue());
                        PlanningList.add(name);

                        dbPlanRef.set(output,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showListItems();
                                Toast.makeText(getActivity(),"workout Added to Group",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }


                }


            }
        });

    }

    private void removeItemFromList()
    {
        if(workoutSelected == null) { return; }

        String value = (String)workoutParent;
        String[] name = value.split(" \\{");
        Log.d("Loggert_Froggert", name[0]);

        Map<String, Object> update = new HashMap<String,Object>();
        update.put(name[0], FieldValue.delete());
        PlanningList.remove(name[0]);


        dbPlanRef.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(getActivity(),"workout Deleted from Group",Toast.LENGTH_SHORT).show();
            }
        });

        workoutSelected.setBackgroundColor(Color.WHITE);
        workoutAdapter.remove(workoutParent);
        workoutAdapter.notifyDataSetChanged();
    }

}