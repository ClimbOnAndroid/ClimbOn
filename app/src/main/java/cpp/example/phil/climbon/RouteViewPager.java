package cpp.example.phil.climbon;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Phil on 3/10/2016.
 */
public class RouteViewPager extends ViewPager {

    public RouteViewPager(Context context){
        super(context);

    }

    public RouteViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /**
     * Override default behavior so the thing isn't so ungodly big. Essentially this
     * enables wrap_content, because xml doesn't seem to want to do that.
     *
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = 0;
        for(int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if(h > height) height = h;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


}
