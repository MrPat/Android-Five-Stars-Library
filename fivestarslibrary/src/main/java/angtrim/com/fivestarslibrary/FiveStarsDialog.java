package angtrim.com.fivestarslibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * Created by angtrim on 12/09/15.
 *
 */
public class FiveStarsDialog implements DialogInterface.OnClickListener{

    private final static String DEFAULT_TITLE = "Rate this app";
    private final static String DEFAULT_TEXT = "How much do you love our app?";
    private final static String DEFAULT_POSITIVE = "Ok";
    private final static String DEFAULT_NEGATIVE = "Not Now";
    private final static String DEFAULT_NEVER = "No thanks";
    private final static String SP_NUM_OF_ACCESS = "numOfAccess";
    private static final String SP_DISABLED = "disabled";
    private static final String TAG = FiveStarsDialog.class.getSimpleName();
    private final Context context;
    private boolean isForceMode = false;
    SharedPreferences sharedPrefs;
    private String supportEmail;
    private TextView contentTextView;
    private RatingBar ratingBar;
    private String title = null;
    private String rateText = null;
    private AlertDialog alertDialog;
    private View dialogView;
    private int upperBound = 4;
    private NegativeReviewListener negativeReviewListener;
    private ReviewListener reviewListener;
    private int starColor;
    private String emailTitle;
    private String emailBody;
    private String appName;


    public FiveStarsDialog(Context context,String supportEmail){
        this.context = context;
        sharedPrefs = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        this.supportEmail = supportEmail;
    }

    private void build(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.stars, null);
        String titleToAdd = (title == null) ? DEFAULT_TITLE : title;
        String textToAdd = (rateText == null) ? DEFAULT_TEXT : rateText;
        contentTextView = (TextView)dialogView.findViewById(R.id.text_content);
        contentTextView.setText(textToAdd);
        contentTextView.setVisibility(View.GONE);
        ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Log.d(TAG, "Rating changed : " + v);
                if (isForceMode && v >= upperBound) {
                    openMarket();
                    if(reviewListener != null)
                        reviewListener.onReview((int)ratingBar.getRating());
                }
            }
        });

        if (starColor != -1){
            LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
            stars.getDrawable(1).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(2).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
        }

        alertDialog = builder.setTitle(titleToAdd)
                .setMessage(textToAdd)
                .setView(dialogView)
                .setNegativeButton(DEFAULT_NEGATIVE,this)
                .setPositiveButton(DEFAULT_POSITIVE,this)
                .setNeutralButton(DEFAULT_NEVER,this)
                .create();
    }

    private void disable() {
        SharedPreferences shared = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(SP_DISABLED, true);
        editor.apply();
    }

    public void enable() {
        SharedPreferences shared = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(SP_DISABLED, false);
        editor.apply();
    }

    private void openMarket() {
        final String appPackageName = context.getPackageName();
        String titleToAdd = (title == null) ? DEFAULT_TITLE : title;

        // Ask the user if they would leave a rating on the app store
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleToAdd)
                .setMessage("Would you like to leave a review on the app store? It really helps me out.")
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create().show();
    }

    private void sendEmail() {
        String titleToAdd = (title == null) ? DEFAULT_TITLE : title;

        // Ask the user if they would leave a rating on the app store
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleToAdd)
                .setMessage("Would you like to send feedback to the developer via email?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nameToUse = appName == null ? context.getPackageName() : appName;
                        String titleToUse = emailTitle == null ? "App Report (" + nameToUse + ")" : emailTitle;
                        String bodyToUse = emailBody == null ? "" : emailBody;

                        try {
                            String mailto = "mailto:" + supportEmail + "?subject=" + titleToUse + "&body=" + bodyToUse.replace("\n","%0D%0A");

                            Log.d(TAG, mailto);

                            final Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mailto));

                            context.startActivity(emailIntent);
                        } catch (Exception e) {
                            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                            emailIntent.setType("text/plain");
                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmail});
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, titleToUse);
                            emailIntent.putExtra(Intent.EXTRA_TEXT, bodyToUse);

                            context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create().show();
    }

    private void show() {
        boolean disabled  = sharedPrefs.getBoolean(SP_DISABLED, false);
        if(!disabled){
            build();
            alertDialog.show();
        }
    }

    public void showAfter(int numberOfAccess){
        build();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int numOfAccess = sharedPrefs.getInt(SP_NUM_OF_ACCESS, 0);
        editor.putInt(SP_NUM_OF_ACCESS, numOfAccess + 1);
        editor.apply();
        if(numOfAccess + 1 >= numberOfAccess){
            show();
        }
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(i == DialogInterface.BUTTON_POSITIVE){
            if(ratingBar.getRating() < upperBound){
                if(negativeReviewListener == null){
                    sendEmail();
                }else{
                    negativeReviewListener.onNegativeReview((int)ratingBar.getRating());
                }

            }else if(!isForceMode){
                openMarket();
            }
            disable();
            if(reviewListener != null)
                reviewListener.onReview((int)ratingBar.getRating());
        }
        if(i == DialogInterface.BUTTON_NEUTRAL){
            disable();
        }
        if(i == DialogInterface.BUTTON_NEGATIVE){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(SP_NUM_OF_ACCESS, 0);
            editor.apply();
        }
        alertDialog.hide();
    }

    public FiveStarsDialog setTitle(String title) {
        this.title = title;
        return this;

    }

    public FiveStarsDialog setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }

    public FiveStarsDialog setRateText(String rateText){
        this.rateText = rateText;
        return this;
    }

    public FiveStarsDialog setStarColor(int color){
        starColor = color;
        return this;
    }

    /**
     * Set to true if you want to send the user directly to the market
     * @param isForceMode
     * @return
     */
    public FiveStarsDialog setForceMode(boolean isForceMode){
        this.isForceMode = isForceMode;
        return this;
    }

    /**
     * Set the upper bound for the rating.
     * If the rating is >= of the bound, the market is opened.
     * @param bound the upper bound
     * @return the dialog
     */
    public FiveStarsDialog setUpperBound(int bound){
        this.upperBound = bound;
        return this;
    }

    /**
     * Set a custom listener if you want to OVERRIDE the default "send email" action when the user gives a negative review
     * @param listener
     * @return
     */
    public FiveStarsDialog setNegativeReviewListener(NegativeReviewListener listener){
        this.negativeReviewListener = listener;
        return  this;
    }

    /**
     * Set a listener to get notified when a review (positive or negative) is issued, for example for tracking purposes
     * @param listener
     * @return
     */
    public FiveStarsDialog setReviewListener(ReviewListener listener){
        this.reviewListener = listener;
        return this;
    }

    public FiveStarsDialog setEmailTitle(String emailTitle) {
        this.emailTitle = emailTitle;
        return this;
    }

    public FiveStarsDialog setEmailBody(String emailBody) {
        this.emailBody = emailBody;
        return this;
    }

    public FiveStarsDialog setAppName(String appName) {
        this.appName = appName;
        return this;
    }
}
