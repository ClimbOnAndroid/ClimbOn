package cpp.example.phil.climbon;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;

/**
 * Created by Phil on 3/8/2016.
 */
public class RouteBuilder {

    private Route newRoute;

    public RouteBuilder(){
        this.newRoute= new Route();
    }


    public Route generateRoute(java.util.Map<String, Object> attributes, Geometry geometry){
         String name;
         int length; //length in feet
         String rating;
         String description;
         Point location;
         String firstAscent;
         String area; //area the route is a part of

        newRoute.setDescription((String)attributes.get("DESCRIPTION"));
        newRoute.setName((String) attributes.get("RTNAME"));
        newRoute.setRating((String) attributes.get("RATING"));
        newRoute.setLocation((Point)geometry);

        return newRoute;




    }
}
