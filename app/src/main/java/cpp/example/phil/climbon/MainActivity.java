package cpp.example.phil.climbon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.SpatialReference;
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
                            List<Route> routes = new ArrayList<Route>();
                            java.util.Map<String, Object> attributes;
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            int i = 0;
                            for (;result.iterator().hasNext();i++) {
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
                                    routes.add(selectedRoute);
                                }

                            }
                            if (!routes.isEmpty()){
                                routeSelected(routes);
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
     * @param selectedRoutes
     */
    private void routeSelected(List<Route> selectedRoutes) {
        for(Route r: selectedRoutes){
            RouteList.INSTANCE.addRoute(r);
        }
        showRouteCallout(theMapView.getCallout(), selectedRoutes);
        theMapView.setViewpointGeometryAsync(selectedRoutes.get(0).getLocation());
    }

    /**
     * This method. Man, this method.  Looks straightforward, right?  Well, let me tell you, it's a headache.
     * It makes a viewpager.  When routes are really close together, many are going to get selected as a result.
     * This leads to the need to see multiple ones and stuff.
     *
     * @param callout
     * @param selectedRoutes
     */
    private void showRouteCallout(Callout callout, final List<Route> selectedRoutes) {
        if (selectedRoutes.size()>1){
            Toast.makeText(this, " Multiple Routes Selected. Swipe bubble to see other routes.", Toast.LENGTH_LONG);
        }
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final RouteViewPager viewPager;
        final CalloutAdapter adapter;

        //check if the view pager happens to be loaded already.  If so, update the data.
        if (callout.getContent() instanceof ViewPager){
            View view = callout.getContent();
            viewPager= (RouteViewPager) findViewById(R.id.viewPager);
            adapter = (CalloutAdapter) viewPager.getAdapter();
            callout.setStyle(new CalloutStyle(this, R.xml.calloutstyle));
            adapter.updateData(selectedRoutes);
            adapter.notifyDataSetChanged();
            }
        else {
            View view = inflater.inflate(R.layout.viewpager, (ViewGroup) callout.getContent(), true);
            viewPager = (RouteViewPager) findViewById(R.id.viewPager);
            adapter = new CalloutAdapter( this, selectedRoutes);
            callout.setStyle(new CalloutStyle(this, R.xml.calloutstyle));
            viewPager.setAdapter(adapter);
            ((ViewGroup) callout.getContent()).removeAllViews();
            callout.setContent(viewPager);
            }
        callout.show();
        //change the location of the callout based on the route being displayed...maybe
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                try {
                    updateCalloutLocation(selectedRoutes.get(position).getLocation());
                } catch (Exception e) {
                    Log.e("Oops", e.getMessage());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    //a method for moving the callout.  It proved necessary so it could be called from nested classes.
    private void updateCalloutLocation(Point location) {
        theMapView.getCallout().setLocation(location);
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
     * This Method will display the callout when called for areas.
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
                            centerOnRoutes(geometries);
                        } catch (Exception e) {
                            Log.e("Error", "Something went wrong.." + e.getMessage());
                        }
                    }
                });

            }
        });

    }

    private void centerOnRoutes(List<Geometry> geometries) {
        double Xmin= Double.MAX_VALUE, Xmax= Double.NEGATIVE_INFINITY, Ymin=Double.MAX_VALUE, Ymax= Double.NEGATIVE_INFINITY, XAverage=0, YAverage=0;
        List<Point> points = new ArrayList<Point>();
        Point newCenter;

        //make the geometries points
        for(Geometry g : geometries){
            if (g.getGeometryType() == GeometryType.POINT){
                points.add((Point) g);
            }
        }
        for(Point p: points){
            XAverage += p.getX();
            YAverage += p.getY();
            if (p.getX() < Xmin){
                Xmin = p.getX();
            }
            if (p.getX() > Xmax){
                Xmax = p.getX();
            }
            if (p.getY() <Ymin){
                Ymin = p.getY();
            }
            if (p.getY() >Ymax){
                Ymax = p.getY();
            }
        }
        XAverage = (Xmax + Xmin)/2;
        YAverage = (Ymax + Ymin)/2;

        double[] newCenterWebMercator = toWebMercator(YAverage, XAverage);
        newCenter = new Point(newCenterWebMercator[0],newCenterWebMercator[1], SpatialReferences.getWebMercator());
        theMapView.setViewpointCenterWithScaleAsync(newCenter, 100000);


    }

    /**
     * The earth is a spheroid. Not a perfect sphere, but kinda close. As such, esri uses a projection
     * that means we need to convert coordinates sometimes. This method does that.  The numbers are derived from
     * some trig, don't question them.
     *
     * @param lat
     * @param log
     * @return
     */
    private double[] toWebMercator(double lat , double log) {
        double mercatorX_lon,  mercatorY_lat;

        if ((Math.abs(log) > (Double) 180.00) || (Math.abs(lat) > 90.00))
            throw new IllegalArgumentException("Out of Range."+ Math.abs(lat)+","+ Math.abs(log));

        double num = log * 0.017453292519943295;
        double x = 6378137.0 * num;
        double a = lat * 0.017453292519943295;

        mercatorX_lon = x;
        mercatorY_lat = 3189068.5 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
        double result[] = {mercatorX_lon,mercatorY_lat};
        return result;
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
