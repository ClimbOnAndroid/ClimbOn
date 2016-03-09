package cpp.example.phil.climbon;

import android.content.Context;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.Feature;
import com.esri.arcgisruntime.datasource.FeatureQueryResult;
import com.esri.arcgisruntime.datasource.QueryParameters;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;

import java.util.ArrayList;

/**
 * Created by Phil on 3/8/2016.
 */
public class RouteMap {
    private static String routesURL =  "http://services6.arcgis.com/nOUvbp8zErFpCAfI/arcgis/rest/services/ClimbingRoutes/FeatureServer/0";
    private static ServiceFeatureTable routeTable = new ServiceFeatureTable(routesURL);

    public static ServiceFeatureTable getRouteTable(Context context){
        if (routeTable == null){
            routeTable = new ServiceFeatureTable(routesURL);
        }
        //routeTable.getOutFields().clear();
        //routeTable.getOutFields().add("*");
        
        return routeTable;
    }







}
