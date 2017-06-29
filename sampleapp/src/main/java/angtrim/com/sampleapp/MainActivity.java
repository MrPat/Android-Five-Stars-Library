package angtrim.com.sampleapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;


public class MainActivity extends Activity implements NegativeReviewListener, ReviewListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this, "angelo.gallarello@gmail.com");
        fiveStarsDialog.enable();
        fiveStarsDialog.setStarColor(Color.YELLOW)
                .setEmailBody("\n\n\nNewline is here\n\nNewline  \n Newline").showAfter(0);
        SharedPreferences shared = getSharedPreferences(getPackageName(), 0);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("numOfAccesses", 0);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNegativeReview(int stars) {
        Log.d(TAG, "Negative review " + stars);
        Toast.makeText(this,"You gave my app a bad review, bas***!!11!!!",Toast.LENGTH_LONG);
    }

    @Override
    public void onReview(int stars) {
        Log.d(TAG, "Review " + stars);
    }
}
