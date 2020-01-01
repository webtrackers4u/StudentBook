package net.reminderbook.studentbook;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;


import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity {
    private Global os;
    private HttpReq http;

    private EditText phone_number_edit_text;
    private ToggleButton password_toggle_button;
    private LinearLayout password_containner;

    ///password
    private EditText password_edit_text_1, password_edit_text_2, password_edit_text_3, password_edit_text_4, password_edit_text_5;
    //progress
    private ProgressBar progress_indicator;

    private Button login_button;
    private  TextView forgot_password_button, register_button;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        os = new Global(getApplicationContext());

        http = new HttpReq(getApplicationContext(), Global.Services.API);
        http.activateDebugging(true);

        if(!os.get_logged_user().matches("")& !os.get_member_details().matches("")){
            openMainActivity();
        } else if(!os.get_logged_user().matches("")& os.get_member_details().matches("")){
            openCompleteProfileActivity();
        } else {
            initializeComponent();
        }
    }

    /********************************
     * Common Functions
     *******************************/
    private void attemptLogin() {
        String mobile = phone_number_edit_text.getText().toString();
        String password = getPassword();

        HttpReq.ParamsModel model = new HttpReq.ParamsModel();
        model.add("member_login", "OK");
        model.add("username", mobile);
        model.add("password", password);

        http.setParams(model);

        http.onStart(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                showProgress();
            }
        });
        http.onSuccess(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                try {
                    JSONObject data = new JSONObject(getData());
                    if(!data.getBoolean("error")){
                        String memberId = data.getString("memberId");
                        os.save_logged_user(memberId);
                        switch (data.getString("status")){
                            case "Active":
                                os.fetch_startup_data(new Runnable() {
                                    @Override
                                    public void run() {
                                        openMainActivity();
                                    }
                                });
                                break;
                            case "Incomplete":
                                openCompleteProfileActivity();
                                break;
                            case "Inactive" :
                                os.toast("Sorry! your account is not verified yet.");
                                break;
                        }
                    } else {
                        os.toast(data.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        http.onError(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                os.toast(getData());;
            }
        });

        http.onEnd(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                dismissProgress();
            }
        });
        http.prepare();
        http.commit(RequestTags.MEMBER_LOGIN_REQUEST);
    }
    private String getPassword(){
        String pass = "";
        pass+=password_edit_text_1.getText();
        pass+=password_edit_text_2.getText();
        pass+=password_edit_text_3.getText();
        pass+=password_edit_text_4.getText();
        pass+=password_edit_text_5.getText();

        return pass;
    }
    /****************************
     * Initialize
     **************************/
    private void initializeComponent(){
        phone_number_edit_text = (EditText) findViewById(R.id.phone_number_edit_text);
        password_containner = findViewById(R.id.password_containner);
        password_edit_text_1 = password_containner.findViewById(R.id.password_edit_text_1);
        password_edit_text_2 = password_containner.findViewById(R.id.password_edit_text_2);
        password_edit_text_3 = password_containner.findViewById(R.id.password_edit_text_3);
        password_edit_text_4 = password_containner.findViewById(R.id.password_edit_text_4);
        password_edit_text_5 = password_containner.findViewById(R.id.password_edit_text_5);

        login_button = (Button) findViewById(R.id.login_button);
        forgot_password_button = (TextView) findViewById(R.id.forgot_password_button);
        register_button = (TextView) findViewById(R.id.register_button);

        progress_indicator = findViewById(R.id.progress_indicator);

        /*********************************
         * Phone Change listener
         *********************************/
        phone_number_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {

            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {
            }

            @Override
            public void afterTextChanged( Editable s ) {
                if(s.toString().length()==10){
                    phone_number_edit_text.setError(null);
                }
            }
        });

        /*********************************
         * Password Change listener
         *********************************/
        password_edit_text_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(password_edit_text_1.getText().length()>0) {
                    password_edit_text_2.requestFocus();
                }
            }
        });
        password_edit_text_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(password_edit_text_2.getText().length()>0) {
                    password_edit_text_3.requestFocus();
                }
            }
        });
        password_edit_text_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(password_edit_text_3.getText().length()>0) {
                    password_edit_text_4.requestFocus();
                }
            }
        });
        password_edit_text_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(password_edit_text_4.getText().length()>0) {
                    password_edit_text_5.requestFocus();
                }
            }
        });
        password_edit_text_5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(password_edit_text_5.getText().length()>0) {
                    password_edit_text_5.clearFocus();
                }
            }
        });

        /***********************
         * Backspace Event
         ***********************/
        password_edit_text_2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_1.requestFocus();
                    }
                }
                return false;
            }
        });
        password_edit_text_3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_2.requestFocus();
                    }
                }
                return false;
            }
        });
        password_edit_text_4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_3.requestFocus();
                    }
                }
                return false;
            }
        });
        password_edit_text_5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_4.requestFocus();
                    }
                }
                return false;
            }
        });

        /**********************
         * password toggler
         **********************/
        password_toggle_button = findViewById(R.id.password_toggle_button);

        password_toggle_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b){
                    password_edit_text_1.setInputType(InputType.TYPE_CLASS_NUMBER);
                    password_edit_text_2.setInputType(InputType.TYPE_CLASS_NUMBER);
                    password_edit_text_3.setInputType(InputType.TYPE_CLASS_NUMBER);
                    password_edit_text_4.setInputType(InputType.TYPE_CLASS_NUMBER);
                    password_edit_text_5.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    password_edit_text_1.setInputType(18);
                    password_edit_text_2.setInputType(18);
                    password_edit_text_3.setInputType(18);
                    password_edit_text_4.setInputType(18);
                    password_edit_text_5.setInputType(18);
                }
            }
        });

        /**********************
         * Button events
         ********************/
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(phone_number_edit_text.getText().length()==10) {
                    attemptLogin();
                } else {
                    phone_number_edit_text.setError("Please input 10 digit phone number");
                }
            }
        });

        forgot_password_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openForgetPasswordActivity();
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegistrationActivity();
            }
        });
    }
    /****************************
     * Intents
     *************************/
    private void openMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void openCompleteProfileActivity(){
        Intent i = new Intent(this, CompleteProfileActivity.class);
        startActivity(i);
        finish();
    }

    private void openForgetPasswordActivity(){
        Intent i = new Intent(this, ForgetPasswordActivity.class);
        startActivity(i);
        finish();
    }

    private void openRegistrationActivity(){
        Intent i = new Intent(this, RegistrationActivity.class);
        startActivity(i);
        finish();
    }

    /*********************************
     * Functions
     ******************************/
    //fetch startup data

    /********************************
     * Request Tags
     *******************************/
    private static class RequestTags {
        static final String MEMBER_LOGIN_REQUEST="RequestTags:MEMBER_LOGIN_REQUEST";
    }

    private void showProgress() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                login_button.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progress_indicator.startAnimation(anim);
    }

    private void dismissProgress(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                login_button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progress_indicator.startAnimation(anim);
    }


}

