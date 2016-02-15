package com.example.phil.climbon;

import android.content.Context;
import android.util.Log;

import com.esri.core.geometry.Geometry;
import com.esri.core.portal.FeatureCollection;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Phil on 2/12/2016.
 */
public class Area {
    private Context context;
    FeatureCollection featureCollection;
    Geometry boundries;
    Firebase data;
    String name;
    Area[] subAreas;


    public Area(Firebase data, Context context) {
        this.data = data;
        this.context =context;

        data.child("0").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot x : dataSnapshot.getChildren()){
                    Log.i("Test","Data" +x+  " : " + x.getClass().getName());

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });




    }

    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }

    public Geometry getBoundries() {
        return boundries;
    }

    public void setBoundries(Geometry boundries) {
        this.boundries = boundries;
    }

    public Firebase getData() {
        return data;
    }

    public void setData(Firebase data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Area[] getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(Area[] subAreas) {
        this.subAreas = subAreas;
    }




}
