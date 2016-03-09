package cpp.example.phil.climbon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.Feature;
import com.esri.arcgisruntime.datasource.FeatureQueryResult;
import com.esri.arcgisruntime.datasource.QueryParameters;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AreaView extends AppCompatActivity {
    List<Route> routes = new ArrayList<Route>();
    private ListView listView;
    private RouteListAdapter routeAdatper ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_view);

        RelativeLayout r = (RelativeLayout) findViewById(R.id.areaView);

        ImageView image = (ImageView) findViewById(R.id.areaimage);
        TextView description = (TextView) findViewById(R.id.areadescription);
        TextView website = (TextView) findViewById(R.id.website);
        website.setMovementMethod(LinkMovementMethod.getInstance());

        Area currentArea =  AreaList.INSTANCE.getAreaByName(getIntent().getStringExtra("Area"));

        Picasso.with(this).load(currentArea.getImageUrl()).into(image);
        description.setText(currentArea.getDescription());
        website.setText(currentArea.getWebUrl());


        this.setTitle(currentArea.getName());


        //populate the routes in the park
        listView = (ListView) findViewById(R.id.routelist);
        routeAdatper = new RouteListAdapter(this, R.layout.route_list_item, routes);
        listView.setAdapter(routeAdatper);

        getRoutesForArea(currentArea);



    }

    public  void getRoutesForArea(Area area){
        final Area queryArea = area;
        final ServiceFeatureTable table =  RouteMap.getRouteTable(this);
        table.getOutFields().add("*");
        table.loadAsync();

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
                            while (result.iterator().hasNext()) {
                                feature = result.iterator().next();
                                Log.i("Route :", feature.getAttributes().toString());
                                java.util.Map<String, Object> attributes;
                                attributes = feature.getAttributes();
                                RouteBuilder routeBuilder = new RouteBuilder();
                                Route newRoute = routeBuilder.generateRoute(attributes, feature.getGeometry());
                                routes.add(newRoute);
                                routeAdatper.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.e("Error", "Something went wrong.."+ e.getMessage());
                        }
                    }
                });

            }
        });



    }


    @Override
    protected void onResume(){
        super.onResume();
    }


    @Override
    protected void onPause(){
        super.onPause();

    }





}


