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
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MealFragment extends Fragment {

    View view;

    ListView headListView;
    ArrayAdapter headAdapter;
    View headSelected;
    Object headParent;

    ListView mealListView;
    ArrayAdapter mealAdapter;
    View mealSelected;
    Object mealParent;

    private FirebaseAuth mAuth;
    private boolean isGuest;
    private FirebaseFirestore db;
    private DocumentReference dbRef;
    private CollectionReference cRef;
    String currentCollection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_meals, container, false);
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
            cRef = db.document("publicData/" + mAuth.getUid()).collection("Meals");
        }

        headListView = (ListView)getView().findViewById(R.id.UserList);
        mealListView = (ListView)getView().findViewById(R.id.GroupList);

        headListView.setNestedScrollingEnabled(true);
        mealListView.setNestedScrollingEnabled(true);

        // Read Head List
        readHead();

        // Select Head List Item
        selectHead();

        // Add Head
        final Button addHBtn = (Button)getView().findViewById(R.id.addGroup);
        addHBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHead();
            }
        });

        // Remove Head
        final Button deleteHBtn = (Button)getView().findViewById(R.id.RemoveHead);
        deleteHBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeHead();
            }
        });

        // View Head
        final Button viewHBtn = (Button)getView().findViewById(R.id.viewHead);
        viewHBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeadItems();
            }
        });

//---------------------------------------//



        // meal List Selection Check
        selectHeadItems();


        // Add Element in meal List
        Button addWBtn = (Button)getView().findViewById(R.id.addMeal);
        addWBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToHead();
            }
        });


        // Delete Element in meal List
        Button deleteWBtn = (Button)getView().findViewById(R.id.deleteWorkout);
        deleteWBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemFromHead();
            }
        });



    }


    private void readHead()
    {
        db.collection("publicData/" + mAuth.getUid() + "/Meals")
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

                dbRef = db.collection("publicData/" + mAuth.getUid()  + "/Meals").document(selectedItem);
                Log.d("Loggert_Froggert", selectedItem);

                showHeadItems();
                notFirst = false;
            }
        });
    }

    private void removeHead()
    {
        if(dbRef == null) { return; }
        dbRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(),"Group Deleted",Toast.LENGTH_SHORT).show();
            }
        });

        headSelected.setBackgroundColor(Color.WHITE);
        headAdapter.remove(headParent);
        headAdapter.notifyDataSetChanged();
    }

    private void addHead()
    {
        EditText HeadName = getView().findViewById(R.id.GroupName);
        if (HeadName.getText().length() == 0) return;
        String name = HeadName.getText().toString();
        Log.d("Loggert_Froggert", "eee");

        HashMap<String, Object> output = new HashMap<>();

        cRef.document(name).set(output).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(),"Group Added",Toast.LENGTH_SHORT).show();
                readHead();
            }
        });
    }

    private void showHeadItems()
    {
        if(dbRef == null) { return; }
        dbRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            ArrayList<String> array = new ArrayList<>();
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){

                    Map<String, Object> s = documentSnapshot.getData();
                    for(Map.Entry<String, Object> entry : s.entrySet())
                    {
                        Log.d("Loggert_Froggert", "eee");
                        String meal = entry.getKey().toString();
                        String setrep = entry.getValue().toString();
                        //                        Log.d("Loggert_Froggert", Integer.toString(array.size()));
                        array.add(meal + " " + setrep);

                    }

                    mealAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                    mealListView.setAdapter(mealAdapter);
                }
            }
        });
    }


    private void selectHeadItems()
    {
        mealListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean notFirst = true;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!notFirst) { mealSelected.setBackgroundColor(Color.WHITE); }

                int scrollY = mealListView.getFirstVisiblePosition();

                mealParent = parent.getItemAtPosition(position);
                String selectedItem = (String) mealParent;

                mealSelected = parent.getChildAt(position-scrollY);
                mealSelected.setBackgroundColor(getResources().getColor(R.color.purple_200));

                notFirst = false;
            }
        });
    }

    private void addItemToHead()
    {
        EditText mealName = getView().findViewById(R.id.MealName);
        EditText SetCount = getView().findViewById(R.id.WeightCount);
        EditText RepCount = getView().findViewById(R.id.CalsCount);


        if ((mealName.getText().length() == 0) ||
                (SetCount.getText().length() == 0) ||
                (RepCount.getText().length() == 0)) return;
        if(dbRef == null) { return; }

        String name = mealName.getText().toString();
        int set = Integer.parseInt(SetCount.getText().toString());
        int rep = Integer.parseInt(RepCount.getText().toString());

        Map<String, Object> map = new HashMap<String,Object>();
        map.put("weight", set);
        map.put("cals", rep);

        HashMap<String, Object> output = new HashMap<>();
        output.put(name, map);

        dbRef.update(output).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(),"meal Added to Group",Toast.LENGTH_SHORT).show();
                showHeadItems();
            }
        });

    }

    private void removeItemFromHead()
    {
        if(mealSelected == null) { return; }

        String value = (String)mealParent;
        String[] name = value.split(" \\{");
        Log.d("Loggert_Froggert", name[0]);

        Map<String, Object> update = new HashMap<String,Object>();
        update.put(name[0], FieldValue.delete());

        dbRef.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(),"meal Deleted from Group",Toast.LENGTH_SHORT).show();
            }
        });

        mealSelected.setBackgroundColor(Color.WHITE);
        mealAdapter.remove(mealParent);
        mealAdapter.notifyDataSetChanged();
    }

}