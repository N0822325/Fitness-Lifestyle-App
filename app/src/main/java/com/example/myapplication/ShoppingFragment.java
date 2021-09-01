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

public class ShoppingFragment extends Fragment {

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
    private DocumentReference dbShopRef;
    String currentCollection;

    ArrayList<String> ShoppingList = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_shopping, container, false);
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
            dbShopRef = db.collection("privateData/" + mAuth.getUid() + "/Shopping").document("List");
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
//                showListItems();
                addItemToList();
            }
        });

//---------------------------------------//

        // Read Shop List
        showListItems();

        // Select Shop List Item
        selectListItems();

        // Delete Element in Shop List
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

                notFirst = false;
            }
        });
    }



    private void showListItems()
    {
        dbShopRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                if(documentSnapshot.exists()){

                    ArrayList<String> array = new ArrayList<>();

                    Map<String, Object> s = documentSnapshot.getData();
                    for(Map.Entry<String, Object> entry : s.entrySet())
                    {
                        Log.d("Loggert_Froggert", "eee");
                        String meal = entry.getKey().toString();
                        String weightcals = entry.getValue().toString();

                        ShoppingList.add(0,meal);
                        array.add(meal + " " + weightcals);

                    }

                    mealAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                    mealListView.setAdapter(mealAdapter);

                }
            }
        });
    }


    private void selectListItems()
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

                        for(String existing : ShoppingList)
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
                        ShoppingList.add(name);

                        dbShopRef.set(output,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showListItems();
                                Toast.makeText(getActivity(),"meal Added to Group",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }


                }


            }
        });

    }

    private void removeItemFromList()
    {
        if(mealSelected == null) { return; }

        String value = (String)mealParent;
        String[] name = value.split(" \\{");
        Log.d("Loggert_Froggert", name[0]);

        Map<String, Object> update = new HashMap<String,Object>();
        update.put(name[0], FieldValue.delete());
        ShoppingList.remove(name[0]);


        dbShopRef.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
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