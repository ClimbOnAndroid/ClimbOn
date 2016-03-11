package cpp.example.phil.climbon;

import java.util.HashMap;

/**
 * Created by Phil on 3/11/2016.
 */
public enum RouteList {

    INSTANCE;

    private static HashMap<String, Route> routes = new HashMap<String, Route>();


    public Route getRouteByName(String name){
        Route route;

        route = routes.get(name);

        return route;
    }

    public void addRoute(Route newRoute){
        routes.put(newRoute.getName(), newRoute);
    }
}
