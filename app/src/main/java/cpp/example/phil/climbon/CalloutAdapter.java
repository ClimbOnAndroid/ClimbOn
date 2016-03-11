package cpp.example.phil.climbon;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Phil on 3/10/2016.
 */



public class CalloutAdapter extends PagerAdapter {

    private final Context context;
    private  List<Route> routes;

    public CalloutAdapter(Context context, List<Route> objects) {
        this.context = context;
        this.routes = objects;

    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View view = layoutInflater.inflate(R.layout.route_callout, container, false);

        TextView name = (TextView) view.findViewById(R.id.route_name);
        TextView description =(TextView) view.findViewById(R.id.routedescription);
        TextView rating = (TextView) view.findViewById(R.id.calloutrating);
        Button detailButton = (Button) view.findViewById(R.id.gotoroutedetails);

        final Route route = routes.get(position);
        Log.i(" Adapter : ", routes.toString());
        detailButton.setText("Details");

        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRouteView(route);
            }
        });
        name.setText(route.getName());
        description.setText(route.getDescription());
        rating.setText(route.getRating());

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return routes.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void updateData(List<Route> updatedRoutes){
        this.routes = updatedRoutes;
    }

    private void goToRouteView(Route route) {
        Intent intent = new Intent(this.context, RouteActivity.class);
        intent.putExtra("Route", route.getName());
        this.context.startActivity(intent);
    }
}
