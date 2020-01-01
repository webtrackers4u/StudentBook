package net.reminderbook.studentbook;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

class Global {
    private Context context;
    private HttpReq http;
    Global(Context ctx ){
        this.context = ctx;
        this.http = new HttpReq(ctx, Services.API);
    }
    private Context getContext() {
        return this.context;
    }

    //SERVER INFO

    static class Services{
        final static String SERVER_ROOT = "http://reminderbook.net/studentbook/";
        final static String API = SERVER_ROOT+"wtosApps/api.php";
        final static String API_Key = "";
    }
    //save cache
    void save_logged_user( String username ) {
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.commit();
    }
    String get_logged_user(){
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        return prefs.getString("username","");
    }
    //SAVE USER DETAILS//
    void save_member_deatils( String memberDeatils ){
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("memberDetails", memberDeatils);
        editor.commit();
    }
    String get_member_details(){
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        return prefs.getString("memberDetails","");
    }
    //SAVE MEMBER SUBJECTS
    void save_member_subjects( String memberDeatils ){
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("memberSubjects", memberDeatils);
        editor.commit();
    }
    String get_member_subjects(){
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        return prefs.getString("memberSubjects","");
    }

    ///SAVE API KEY
    void save_api_key( String api_key ){
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("api_key", api_key);
        editor.commit();
    }
    String get_api_key(){
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        return prefs.getString("api_key","");
    }

    /***********************************
     * Image Functions
     *********************************/
    void setImage(final ImageView viewToSetImage, final String fileName , Boolean roundCorner){
        if(!fileName.isEmpty()) {
            Picasso.get()
                    .load(Services.SERVER_ROOT + fileName)
                    //.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .placeholder(R.drawable.ajax_loader)
                    .error(R.drawable.ic_error_outline)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                            if(bitmap.getWidth()>viewToSetImage.getWidth()){
                                viewToSetImage.setAdjustViewBounds(true);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    viewToSetImage.setCropToPadding(true);
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    viewToSetImage.setCropToPadding(false);
                                }
                                viewToSetImage.setAdjustViewBounds(false);
                                viewToSetImage.setScaleType(ImageView.ScaleType.FIT_START);
                            }


                            viewToSetImage.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            viewToSetImage.setImageDrawable(errorDrawable);
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            viewToSetImage.setImageDrawable(placeHolderDrawable);
                        }
                    });
        }


    }
    public static class PicassoImageGetter implements Html.ImageGetter {

        private TextView textView = null;
        private Context context = null;

        public PicassoImageGetter() {

        }

        public PicassoImageGetter(TextView target, Context ctx) {
            textView = target;
            this.context = ctx;
        }

        public Context getContext() {
            return context;
        }

        @Override
        public Drawable getDrawable(String source) {
            BitmapDrawablePlaceHolder drawable = new BitmapDrawablePlaceHolder();
            Picasso.get()
                    .load(source)
                    .placeholder(R.drawable.ajax_loader)
                    .into(drawable);
            return drawable;
        }



        private class BitmapDrawablePlaceHolder extends BitmapDrawable implements Target {

            protected Drawable drawable;

            @Override
            public void draw(final Canvas canvas) {
                if (drawable != null) {
                    drawable.draw(canvas);
                }
            }

            public void setDrawable(Drawable drawable) {
                this.drawable = drawable;
                float defaultProportion = (float) drawable.getIntrinsicWidth() / (float) drawable.getIntrinsicHeight();
                int width = Math.min(textView.getWidth(), drawable.getIntrinsicWidth());
                int height = (int) ((float) width / defaultProportion);
                /*
                drawable.setBounds(0, 0, width, height);
                setBounds(0, 0, width, height);
                if (textView != null) {
                    textView.setText(textView.getText());
                }
                */
                if (getBounds().right != textView.getWidth() || getBounds().bottom != height) {

                    setBounds(0, 0, textView.getWidth(), height); //set to full width

                    int halfOfPlaceHolderWidth = (int) ((float) getBounds().right / 2f);
                    int halfOfImageWidth = (int) ((float) width / 2f);

                    drawable.setBounds(
                            halfOfPlaceHolderWidth - halfOfImageWidth, //centering an image
                            0,
                            halfOfPlaceHolderWidth + halfOfImageWidth,
                            height);

                    textView.setText(textView.getText()); //refresh text
                }
            }

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                setDrawable(new BitmapDrawable(getContext().getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }


            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        }
    }

    /**********************************
     * String function
     **********************************/
    void setText( TextView viewToSetText, String value ) {
        if(!value.isEmpty()) {
            String text =  value.replaceAll("\\s+"," ").replaceAll("(?i)<br */?>","\n");
            viewToSetText.setText(text);
        }
    }
    void setTextWithImage(TextView view_to_set_text, String htmlString){
        htmlString = htmlString.replaceAll("\\\\", "");
        Global.PicassoImageGetter imageGetter = new Global.PicassoImageGetter( view_to_set_text, getContext());
        HtmlTagHandler htmlTagHandler = new HtmlTagHandler();
        Spannable html;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            html = (Spannable) Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY, imageGetter, htmlTagHandler);
        } else {
            html = (Spannable) Html.fromHtml(htmlString, imageGetter, htmlTagHandler);
        }
        view_to_set_text.setText(html);
    }

    public class HtmlTagHandler implements Html.TagHandler{

        @Override
        public void handleTag(boolean b, String s, Editable editable, XMLReader xmlReader) {
            if(s.equalsIgnoreCase("p")) {
                processStrike(b, editable);
            }
        }

        private void processStrike(boolean opening, Editable output) {
            int len = output.length();
            if(opening) {
                output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
            } else {
                Object obj = getLast(output, StrikethroughSpan.class);
                int where = output.getSpanStart(obj);

                output.removeSpan(obj);

                if (where != len) {
                    output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        private Object getLast(Editable text, Class kind) {
            Object[] objs = text.getSpans(0, text.length(), kind);

            if (objs.length == 0) {
                return null;
            } else {
                for(int i = objs.length;i>0;i--) {
                    if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
                        return objs[i-1];
                    }
                }
                return null;
            }
        }
    }
    /*******************************
     * Native Functions
     ******************************/
    void setStatusBarColor( Activity activity, int color ){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }
    void toast( String text ){
        Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
    }

    static class AddClearAbleButton{
        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_TOP = 1;
        final int DRAWABLE_RIGHT = 2;
        final int DRAWABLE_BOTTOM = 3;
        AddClearAbleButton(final View view, final Runnable callback){
            final EditText inputbox = (EditText)view;
            inputbox.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_LEFT = 0;
                    final int DRAWABLE_TOP = 1;
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;

                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        if (inputbox.getText().length()>0) {
                            if (event.getRawX() >= (inputbox.getRight() - inputbox.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                // your action here
                                if (inputbox.getText().length() > 0) {
                                    inputbox.setText(null);
                                    callback.run();
                                }
                                return true;
                            }

                        }
                    }
                    return false;
                }
            });
            inputbox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(editable.toString().length()>0){
                        inputbox.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_black_24dp, 0);
                        inputbox.setCompoundDrawablePadding(15);
                    } else {
                        inputbox.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    }
                }
            });
        }



    }
    int getRootWidth(int percenatge){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        float fraction = (float)percenatge / 100;
        toast(String.valueOf(fraction));
        return (int)(width*fraction);
    }
    /*******************************
     * Convertions
     *******************************/
    public float DpToPx(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
    /*******************************
     * HTTP SERVICES
     *********************************/
    public void sendOTP(final String mobile, final String OTP){
        if(mobile.length()==10) {
            Toast.makeText(getContext(), OTP, Toast.LENGTH_SHORT).show();
            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url = Global.Services.API;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject data = new JSONObject(response);
                                String status = data.getString("status");
                                if(status.matches("OK")){
                                    Toast.makeText(getContext(), "Password sent successfully",Toast.LENGTH_SHORT).show();
                                }
                                Toast.makeText(getContext(), data.toString(),Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse( VolleyError error) {
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("send_otp", "OK");
                    params.put("mobile", mobile);
                    params.put("otp", OTP);
                    return params;
                }
            };
            queue.add(postRequest);
        }
    }
    public void fetch_startup_data(final Runnable callback){

        HttpReq.ParamsModel model = new HttpReq.ParamsModel();
        model.add("fetch_startup_data", "OK");
        model.add("memberId", get_logged_user());

        http.setParams(model);

        http.onSuccess(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                try {
                    JSONObject data = new JSONObject(getData());
                    if(data.getJSONObject("memberDetails").getString("status").matches("Incomplete")){

                    } else {
                        save_member_deatils(data.getJSONObject("memberDetails").toString());
                        save_member_subjects(data.getJSONArray("subjects").toString());
                    }
                    callback.run();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        http.onError(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                Log.d("Error.Response", getData().toString());
            }
        });

        http.prepare();
        http.commit("Cool");
    }
}


