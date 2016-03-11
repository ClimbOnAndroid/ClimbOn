package cpp.example.phil.climbon;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Phil on 3/8/2016.
 */
public class RouteListAdapter extends ArrayAdapter<Route>{


    private final Context context;
    private final int resource;
    private final List<Route> routes;

    public RouteListAdapter(Context context, int resource, List<Route> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.routes = objects;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.i("Test", "getView() called at position " + position);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View view = layoutInflater.inflate(R.layout.route_list_item, parent, false);


        final Route route = routes.get(position);

        TextView routeName = (TextView) view.findViewById(R.id.routename);
        routeName.setText(route.getName());

        TextView rating = (TextView) view.findViewById(R.id.rating);
        rating.setText( route.getRating());


        return view;

    }
}
