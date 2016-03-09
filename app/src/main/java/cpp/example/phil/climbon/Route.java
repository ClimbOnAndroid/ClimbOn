package cpp.example.phil.climbon;

import com.esri.arcgisruntime.geometry.Point;

/**
 * Created by phils on 2/18/2016.
 */
public class Route {
    private String name;
    private int length; //length in feet
    private double rating;
    private String description;
    private Point location;
    private String firstAscent;
    private String area; //area the route is a part of


    public Route(){

    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getFirstAscent() {
        return firstAscent;
    }

    public void setFirstAscent(String firstAscent) {
        this.firstAscent = firstAscent;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }


}
