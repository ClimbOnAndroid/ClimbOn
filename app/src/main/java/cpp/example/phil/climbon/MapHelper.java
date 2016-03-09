package cpp.example.phil.climbon;

import android.content.Context;

import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;

/**
 * Created by Phil on 3/8/2016.
 */
public class MapHelper {
    final static Map map = new Map(Basemap.createImagery());

    public static Map getMap(Context context){
        return map;
    }
}
