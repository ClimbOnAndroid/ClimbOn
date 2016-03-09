package cpp.example.phil.climbon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.Feature;
import com.esri.arcgisruntime.datasource.FeatureQueryResult;
import com.esri.arcgisruntime.datasource.QueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.CalloutStyle;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private final int VIEW_AREAS=0;
    private final int VIEW_ROUTES=1;
    private int currentMapBehavior;
    protected Location theLocation;
    protected Point userLocation;
    protected MapView theMapView;
    protected LocationDisplay theManager;
    private ProgressDialog progressDialog;
    private AtomicInteger count;
    private FeatureLayer areaLayer, routeLayer;
    private ServiceFeatureTable areaTable,routeTable;
    private FloatingActionButton returnToAreas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentMapBehavior = VIEW_AREAS;

        String URL = "http://services6.arcgis.com/nOUvbp8zErFpCAfI/arcgis/rest/services/Areas/FeatureServer/0";
        String routesURL =  "http://services6.arcgis.com/nOUvbp8zErFpCAfI/arcgis/rest/services/ClimbingRoutes/FeatureServer/0";
        areaTable = new ServiceFeatureTable(URL);
        routeTable = RouteMap.getRouteTable(this);
        areaTable.getOutFields().clear();
        areaTable.getOutFields().add("*");

        areaLayer = new FeatureLayer(areaTable);
        routeLayer = new FeatureLayer(routeTable);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initialize the map and the userLocationManager
        theMapView = (MapView) findViewById(R.id.map);
        theMapView.setMap(MapHelper.getMap(this));
        theManager = theMapView.getLocationDisplay();
        theManager.start();
        MapHelper.getMap(this).getOperationalLayers().add(areaLayer);


        // zoom to a view point of the USA
        theMapView.setViewpointCenterWithScaleAsync(new Point(-11000000, 5000000, SpatialReferences.getWebMercator()), 100000000);


        theMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, theMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // get the point that was clicked and convert it to a point in map coordinates
                final Point clickPoint = theMapView.screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                theMapView.getCallout().setLocation(clickPoint);
                int tolerance = 44;
                double mapTolerance = tolerance * theMapView.getUnitsPerPixel();

                // create objects required to do a selection with a query
                Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance, clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, 0, 0, 0, 0, MapHelper.getMap(this.mContext).getSpatialReference());
                QueryParameters query = new QueryParameters();
                query.setGeometry(envelope);
                query.getOutFields().add("*");
                Callout callout = theMapView.getCallout();
                //hide the callout on a new click
                if (callout.isShowing()) {
                    callout.hide();
                }
                callout.setLocation(clickPoint);

                final ListenableFuture<FeatureQueryResult> future;
                if (currentMapBehavior == VIEW_AREAS) {
                    // call select features
                    future = areaLayer.selectFeatures(query, FeatureLayer.SelectionMode.NEW);
                } else {
                    future = routeLayer.selectFeatures(query, FeatureLayer.SelectionMode.NEW);
                }

                // add done loading listener to fire when the selection returns
                future.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Feature selectedFeature;
                            java.util.Map<String, Object> attributes;
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            if (result.iterator().hasNext()) {
                                Log.i("Test", "Feature: " + result.toString());
                                selectedFeature = result.iterator().next();
                                Log.i("Select", selectedFeature.getAttributes().toString());
                                attributes = selectedFeature.getAttributes();
                                Log.i("attributes", attributes.toString());
                                if (currentMapBehavior == VIEW_AREAS) {
                                    AreaBuilder builder = new AreaBuilder();
                                    Area selectedArea = builder.generateArea(attributes, selectedFeature.getGeometry());
                                    areaSelected(selectedArea);
                                } else {
                                    RouteBuilder builder = new RouteBuilder();
                                    Route selectedRoute = builder.generateRoute(attributes, selectedFeature.getGeometry());
                                    routeSelected(selectedRoute);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Error", "Select feature failed: " + e.getMessage());
                        }
                    }
                });
                return super.onSingleTapConfirmed(e);
            }
        });


        //The Button that focuses on your location, exciting.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (theManager.isStarted()) {
                    Log.i("Location", theManager.getMapLocation().toString());
                    Log.i("Location2", theManager.getLocation().toString());
                    userLocation = theManager.getMapLocation();
                    Log.i("UserLocation", userLocation.toString());
                    if(userLocation!=null)
                        theMapView.setViewpointCenterWithScaleAsync(userLocation, 1000000);
                    Snackbar.make(view, "Moving to your location.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(view, "Still Acquiring location. Please Wait.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        returnToAreas = (FloatingActionButton) findViewById(R.id.returnbutton);
        returnToAreas.setClickable(false);
        returnToAreas.setVisibility(View.GONE);
        returnToAreas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theMapView.getCallout().hide();
                returnToAreas.setVisibility(View.GONE);
                returnToAreas.setClickable(false);
                currentMapBehavior = VIEW_AREAS;
                MapHelper.getMap(getApplicationContext()).getOperationalLayers().add(areaLayer);
                MapHelper.getMap(getApplicationContext()).getOperationalLayers().remove(routeLayer);
                theMapView.setViewpointScaleAsync(10000000);
            }
        });
    }

    /**
     *
     * @param selectedRoute
     */
    private void routeSelected(Route selectedRoute) {
        showRouteCallout(theMapView.getCallout(), selectedRoute);
        theMapView.setViewpointGeometryAsync(selectedRoute.getLocation());
    }

    /**
     *
     * @param callout
     * @param selectedRoute
     */
    private void showRouteCallout(Callout callout, final Route selectedRoute) {
        LayoutInflater inflater = this.getLayoutInflater();
        int xmlid = R.xml.calloutstyle;
        View contentView = inflater.inflate(R.layout.route_callout, null);
        callout.setStyle(new CalloutStyle(this, R.xml.calloutstyle));
        callout.setContent(contentView);
        TextView name = (TextView) contentView.findViewById(R.id.route_name);
        TextView description =(TextView) contentView.findViewById(R.id.routedescription);
        TextView rating = (TextView) contentView.findViewById(R.id.calloutrating);
        Button detailButton = (Button) contentView.findViewById(R.id.gotoroutedetails);

        detailButton.setText("Details");

        detailButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToRouteView(selectedRoute);
            }
        });
        name.setText(selectedRoute.getName());
        description.setText(selectedRoute.getDescription());
        rating.setText(Double.toString(selectedRoute.getRating()));


        callout.show();
    }

    /**
     *
     * @param selectedRoute
     */
    private void goToRouteView(Route selectedRoute) {

    }

    /**
     *
     * @param selectedArea
     */
    public void areaSelected(Area selectedArea){
        AreaList.INSTANCE.addArea(selectedArea);
        showAreaCallout(theMapView.getCallout(), selectedArea);
        theMapView.setViewpointGeometryAsync(selectedArea.getBoundries());

    }

    /**
     * This Method will display the callout when called, probably should make a method that hides it.
     *
     * @param callout
     * @param selectedArea
     */
    private void showAreaCallout(Callout callout, final Area selectedArea) {
        LayoutInflater inflater = this.getLayoutInflater();
        int xmlid = R.xml.calloutstyle;
        View contentView = inflater.inflate(R.layout.area_callout, null);
        callout.setStyle(new CalloutStyle(this, R.xml.calloutstyle));
        callout.setContent(contentView);
        TextView name = (TextView) contentView.findViewById(R.id.name);
        TextView description =(TextView) contentView.findViewById(R.id.description);
        Button zoomButton = (Button) contentView.findViewById(R.id.zoomintoroutes);
        Button detailButton = (Button) contentView.findViewById(R.id.gotodetails);

        detailButton.setText("Details");
        zoomButton.setText("Zoom In");
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToRoutes(selectedArea);
            }
        });
        detailButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToAreaView(selectedArea);
            }
        });
        name.setText(selectedArea.getName());
        description.setText(selectedArea.getDescription());


        callout.show();
    }

    /**
     * This methos starts a new view that is going to get all the data from the selected area and
     * display it in a lovely UI.  Lovely, I tell you.  Alright, lovely is a stretch. I'm not a graphic
     * designer.
     *
     * @param selectedArea
     */
    private void goToAreaView(Area selectedArea) {
        Intent intent = new Intent(this, AreaView.class);
        intent.putExtra("Area", selectedArea.getName());
        startActivity(intent);
    }

    /**
     * This method will zoom the map into the selected area and show the views.  I meaan,
     * that is what is should do.  Milage may vary on it's actual performance.
     *
     * @param selectedArea
     */
    private void zoomToRoutes(Area selectedArea) {
        currentMapBehavior = VIEW_ROUTES;
        returnToAreas.setClickable(true);
        theMapView.getCallout().hide();
        final Area queryArea = selectedArea;
        final ServiceFeatureTable table =  RouteMap.getRouteTable(this);
        returnToAreas.setVisibility(View.VISIBLE);

        if(table.getLoadStatus()== LoadStatus.NOT_LOADED) {
            table.getOutFields().add("*");
            table.loadAsync();
        }

        MapHelper.getMap(this).getOperationalLayers().remove(areaLayer);
        MapHelper.getMap(this).getOperationalLayers().add(routeLayer);

        table.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                QueryParameters parameters = new QueryParameters();
                parameters.getOutFields().add("*");
                parameters.setGeometry(queryArea.getBoundries());


                final ListenableFuture<FeatureQueryResult> future = table.queryFeaturesAsync(parameters);
                future.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Feature feature;
                            FeatureQueryResult result = future.get();
                            List<Route> routeList = new ArrayList<Route>();
                            List<Geometry> geometries = new ArrayList<Geometry>();
                            while (result.iterator().hasNext()) {
                                feature = result.iterator().next();
                                geometries.add(feature.getGeometry());
                                routeLayer.setFeatureVisible(feature, true);
                            }
                            //theMapView.setViewpointCenterWithScaleAsync(calculateBounds(geometries).getCenter(), 1000);
                        } catch (Exception e) {
                            Log.e("Error", "Something went wrong.." + e.getMessage());
                        }
                    }
                });

            }
        });

    }

    private Envelope calculateBounds(List<Geometry> geometries) {
        Envelope bounds;
        double Xmax= Double.MIN_VALUE, Xmin = Double.MAX_VALUE, Ymax = Double.MAX_VALUE , Ymin= Double.MIN_VALUE;

        for(Geometry g : geometries){
           if( g.getExtent().getXMax() > Xmax)
               Xmax = g.getExtent().getXMax();
           if ( g.getExtent().getXMin() < Xmin)
                Xmin = g.getExtent().getXMin();
           if( g.getExtent().getYMax() > Ymax)
               Ymax = g.getExtent().getYMax() ;
            if (g.getExtent().getYMin()<Ymin)
               Ymin = g.getExtent().getYMin() ;
        }

        bounds = new Envelope(Xmin, Ymin, Xmax, Ymax, 0, 0, 0, 0, MapHelper.getMap(this).getSpatialReference() );
        return bounds;
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
        theMapView.resume();
        if(!theManager.isStarted()){
             theManager.start();
            }
        }


    @Override
    protected void onPause(){
        super.onPause();
        theMapView.pause();
        if(theManager.isStarted()){
            theManager.stop();
        }
    }




}
