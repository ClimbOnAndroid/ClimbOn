package cpp.example.phil.climbon;

import android.content.Context;

import com.esri.arcgisruntime.geometry.Geometry;
import com.firebase.client.Firebase;

import java.io.Serializable;

/**
 * Created by Phil on 2/12/2016.
 */
public class Area  {
    private Context context;

    Geometry boundries;
    Firebase data;
    String name;
    Long id;
    String description;
    Area[] subAreas;
    String webUrl;
    String imageUrl;

    public Area(){

    }

    public Area(String name, Long ID, String description) {
        this.name = name;
        this.description = description;
        this.id = ID;

    }


    public String getDescription(){return description;}

    public void setDescription(String description){this.description = description;}

    public Geometry getBoundries() {
        return boundries;
    }

    public void setBoundries(Geometry boundries) {
        this.boundries = boundries;
    }

    public Firebase getData() {
        return data;
    }

    public void setData(Firebase data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Area[] getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(Area[] subAreas) {
        this.subAreas = subAreas;
    }

    public String getWebUrl(){
        return webUrl;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public void setId(Long id){this.id = id;}

    public void setWebUrl(String url){
        this.webUrl = url;
    }

    public void setimageUrl(String url){
        this.imageUrl = url;
    }



}
