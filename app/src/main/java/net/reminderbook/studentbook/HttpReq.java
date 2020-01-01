package net.reminderbook.studentbook;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpReq {
    private Context context;
    private String url;
    //API DETAILS
    String API_KEY = "";
    private StringRequest request;
    private RequestQueue queue;
    //parameters
    private Map<String,String> httpParams;
    //events
    private CallbackListener successListener=null;
    private CallbackListener errorListener=null;
    private CallbackListener startListener=null;
    private CallbackListener endListener=null;

    private boolean debug_mode=false;

    HttpReq( Context context, String url) {
        this.context = context;
        queue = Volley.newRequestQueue(context);
        this.url = url;
    }

    public Context getContext() {
        return this.context;
    }
    //API KEY
    void set_api_key(String API_KEY) {
        this.API_KEY = API_KEY;
    }


    //Events
    void onSuccess( CallbackListener callback ){
        this.successListener = callback;
    }
    void onError(CallbackListener callback){this.errorListener  = callback;}
    void onStart(CallbackListener callback){this.startListener=callback;}
    void onEnd(CallbackListener callback){this.endListener = callback;}
    //set parameners
    void setParams(ParamsModel model) { this.httpParams = model.getParams(); }
    void activateDebugging(boolean mode){ this.debug_mode=mode; }

    void prepare(){
        request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(debug_mode){
                            Log.d("HttpReq/Response",response);
                        }

                        successListener.setData(response);
                        successListener.run();

                        if (endListener!=null){
                            endListener.run();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse( VolleyError error) {
                        Log.d("Error.Response", error.toString());

                        if (errorListener!=null){
                            errorListener.data = error.getMessage();
                            errorListener.run();
                        }
                        if (endListener!=null){
                            endListener.data = error.getMessage();
                            endListener.run();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String credentials = "ahmednafish7@gmail.com" + ":" + "123";
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("apiKey", API_KEY);
                return headers;
            }
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = httpParams;
                long timeStamp = (System.currentTimeMillis()/1000);
                params.put("stamp", Long.toString(timeStamp));
                return params;
            }
        };


    }

    void commit(Object tag){
        if (startListener != null){
           startListener.run();
        }
        request.setTag(tag);
        queue.add(request);
    }

    static class CallbackListener implements Runnable {
        private String data;

        public void run() { }
        void setData(String data){
            this.data = data;
        }
        String getData(){
            return this.data;
        }
    }
    //cancel req
    public void calcelReq(Object tag) {
        queue.cancelAll(tag);
    }

    //Parameter model class.
    static class ParamsModel{
        private Map<String, String> map = new HashMap<String, String>();

        void add(String key, String val){
            map.put(key,val);
        }
        Map<String,String> getParams(){
            return this.map;
        }
    }
}


