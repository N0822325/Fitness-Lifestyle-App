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
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

public class CommunityFragment extends Fragment {

    View view;

    ListView userListView;
    ArrayAdapter userAdapter;
    View userSelected;
    Object userParent;

    ListView groupListView;
    ArrayAdapter groupAdapter;
    View groupSelected;
    Object groupParent;

    ListView mealListView;
    ArrayAdapter mealAdapter;
    View mealSelected;
    Object mealParent;

    private FirebaseAuth mAuth;
    private boolean isGuest;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private DocumentReference groupRef;
    private CollectionReference cRef;
    private DocumentReference dbShopRef;
    String currentCollection;

    ArrayList<String> ShoppingList = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_community, container, false);
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

        userListView = (ListView)getView().findViewById(R.id.UserList);
        groupListView = (ListView)getView().findViewById(R.id.GroupList);
        mealListView = (ListView)getView().findViewById(R.id.MealList);

        userListView.setNestedScrollingEnabled(true);
        groupListView.setNestedScrollingEnabled(true);
        mealListView.setNestedScrollingEnabled(true);

        // Read Head List
        readHead();

        // Select Head List Item
        selectHead();

        // Select Group List Item
        selectGroupItems();

        // Add Head
//        final Button addHBtn = (Button)getView().findViewById(R.id.addGroup);
//        addHBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                showListItems();
////                addItemToList();
//            }
//        });

//---------------------------------------//
//
//        // Read Shop List
//        showListItems();
//
//        // Select Shop List Item
//        selectListItems();
//
//        // Delete Element in Shop List
//        Button deleteWBtn = (Button)getView().findViewById(R.id.deleteWorkout);
//        deleteWBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                removeItemFromList();
//            }
//        });



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

                                mealAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, new ArrayList());
                                mealListView.setAdapter(mealAdapter);

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

        userRef.collection("Meals").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                array.add(document.getId());

                                Log.d("TAG",document.getId() + " => " + document.getData());
//                                Toast.makeText(getActivity(),document.getId(),Toast.LENGTH_SHORT).show();
                            }


                            userRef.collection("Workouts").get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    array.add(document.getId());

                                                    Log.d("TAG",document.getId() + " => " + document.getData());
//                                Toast.makeText(getActivity(),document.getId(),Toast.LENGTH_SHORT).show();
                                                }
                                                groupAdapter = new ArrayAdapter(getActivity(), R.layout.list_item, array);
                                                groupListView.setAdapter(groupAdapter);
                                            } else {
                                                Log.d("TAG", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });


                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }


                    }
                });
    }


    private void selectGroupItems()
    {
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean notFirst = true;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!notFirst) { groupSelected.setBackgroundColor(Color.WHITE); }

                int scrollY = groupListView.getFirstVisiblePosition();

                groupParent = parent.getItemAtPosition(position);
                String selectedItem = (String) groupParent;

                groupSelected = parent.getChildAt(position-scrollY);
                groupSelected.setBackgroundColor(getResources().getColor(R.color.purple_200));

                userRef.collection("Meals").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            String path = "Workouts";
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                               for (QueryDocumentSnapshot document : task.getResult()) {
                                   Log.d("ggggggggg", document.getId());
                                   if (document.getId().equals(selectedItem)) {
                                       path = "Meals";
                                   }
                               }

                               groupRef = userRef.collection(path).document(selectedItem);
                               showGroupItems();
                               notFirst = false;
                           }
                       });


            }
        });
    }

    private void showGroupItems()
    {

        groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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

}
