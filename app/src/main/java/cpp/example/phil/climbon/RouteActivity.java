package cpp.example.phil.climbon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Phil on 3/11/2016.
 */
public class RouteActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_view);

        LinearLayout l = (LinearLayout) findViewById(R.id.routelayout);


        TextView description = (TextView) findViewById(R.id.descriptiontext);
        TextView rating = (TextView) findViewById(R.id.routerating);
        TextView name = (TextView) findViewById(R.id.routename2);


        Route currentRoute =  RouteList.INSTANCE.getRouteByName(getIntent().getStringExtra("Route"));

        description.setText(currentRoute.getDescription());
        rating.setText(currentRoute.getRating());
        name.setText(currentRoute.getName());




        this.setTitle(currentRoute.getName());


    }

}
