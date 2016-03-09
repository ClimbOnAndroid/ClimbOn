package cpp.example.phil.climbon;

import com.esri.arcgisruntime.geometry.Geometry;

/**
 * Created by phils on 2/18/2016.
 */
public class AreaBuilder {

    private Area newArea;

    public AreaBuilder(){
    this.newArea = new Area();
    }


    public Area generateArea(java.util.Map<String, Object> attributes, Geometry geometry) {
        String description;
        String name;
        Long id;
        String webUrl;
        String imageUrl;

        newArea.setDescription((String) attributes.get("DESCRIPTION"));
        newArea.setName((String) attributes.get("Park"));
        newArea.setId((Long) attributes.get("OBJECTID"));
        newArea.setWebUrl((String) attributes.get("webURL"));
        newArea.setimageUrl((String) attributes.get("imgURL"));

        newArea.setBoundries(geometry);

        return newArea;

    }




}
