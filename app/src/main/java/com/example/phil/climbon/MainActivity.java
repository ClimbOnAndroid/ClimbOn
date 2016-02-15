package com.example.phil.climbon;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.geometry.Point;
import com.firebase.client.Firebase;

public class MainActivity extends AppCompatActivity {

    protected Location theLocation;
    protected Point userLocation;
    protected MapView theMap;
    protected LocationDisplayManager theManager;
    protected ArcGISFeatureLayer fLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String URL = "http://services6.arcgis.com/nOUvbp8zErFpCAfI/arcgis/rest/services/Areas/FeatureServer/0";
        fLayer = new ArcGISFeatureLayer(URL, ArcGISFeatureLayer.MODE.ONDEMAND );
        Log.i("Test", fLayer.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        Firebase joshuaTree = new Firebase("https://fiery-inferno-8487.firebaseio.com/");

        Area jTree = new Area(joshuaTree, MainActivity.this );
        //initialize the map and the userLocationManager
        theMap = (MapView) findViewById(R.id.map);
        theManager = theMap.getLocationDisplayManager();
        theManager.start();
        fLayer.setVisible(true);
        theMap.addLayer(fLayer);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(theManager.isStarted()){
                    theLocation = theManager.getLocation();
                    theMap.centerAt(theLocation.getLatitude(), theLocation.getLongitude(), true);
                    theMap.zoomin(true);
                    theManager.setShowLocation(true);
                    Snackbar.make(view, "Moving to your location.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else{
                    Snackbar.make(view, "Still Acquiring location. Please Wait.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        theManager.resume();
    }


    @Override
    protected void onPause(){
        super.onPause();
        theManager.pause();
    }




}
