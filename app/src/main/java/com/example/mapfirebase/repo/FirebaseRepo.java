package com.example.mapfirebase.repo;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.mapfirebase.MapsActivity;
import com.example.mapfirebase.model.MyLocation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseRepo {

    public static List<MyLocation> locations = new ArrayList<>();
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static MapsActivity mapsActivity;
    private static String path = "mylocations";

    public static void setMapsActivity(MapsActivity activity){
        mapsActivity = activity;
        // start listener here, because now the parent is ready.
        startListener(); // temporary solution
    }


    public static void addMarker(String title, double lat, double lon){
        DocumentReference ref = db.collection(path).document();
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("lat", String.valueOf(lat));
        map.put("lon", String.valueOf(lon));
        ref.set(map); // this will trigger the listener to load all the data.
    }

    private static void startListener(){
        db.collection(path).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot data, @Nullable FirebaseFirestoreException e) {
                locations.clear(); // empty the list first
                for(DocumentSnapshot snap : data.getDocuments()){
                    MyLocation location = new MyLocation(snap.get("lat").toString(), snap.get("lon").toString());
                    locations.add(location);
                }
                mapsActivity.updateMarkers(); // call "parent" and ask it to update the map.
            }
        });

    }




}