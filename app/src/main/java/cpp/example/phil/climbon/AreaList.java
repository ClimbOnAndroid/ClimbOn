package cpp.example.phil.climbon;


import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Phil on 3/8/2016.
 */
public enum AreaList {

    INSTANCE;

    private static HashMap<String, Area> Areas = new HashMap<String, Area>();


    public Area getAreaByName(String name){
        Area area;

        area = Areas.get(name);

        return area;
    }

    public void addArea(Area newArea){
        Areas.put(newArea.getName(), newArea);
    }


}
