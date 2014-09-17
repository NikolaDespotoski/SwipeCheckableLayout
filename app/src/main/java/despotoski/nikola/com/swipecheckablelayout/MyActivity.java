package despotoski.nikola.com.swipecheckablelayout;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.nikola.despotoski.swipecheckablelayout.SwipeCheckableLayout;


public class MyActivity extends Activity implements SwipeCheckableLayout.OnSwipeListener {

    private SwipeCheckableLayout mSwipeCheckableLayout;
    private int mCheckedColor;
    private int mUncheckedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mSwipeCheckableLayout = (SwipeCheckableLayout) findViewById(R.id.swipe_checkable_layout);
        mSwipeCheckableLayout.setOnSwipeListener(this);
        mCheckedColor = Color.GREEN;
        mUncheckedColor = Color.RED;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSwipe(boolean isChecked, float offset) {
        mSwipeCheckableLayout.setBackground(new ColorDrawable(isChecked? mCheckedColor: mUncheckedColor));
    }

    @Override
    public void onSwipeStateChanged(int state) {

    }
}
