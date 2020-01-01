package net.reminderbook.studentbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationActivity extends AppCompatActivity {
    ViewFlipper main_view_flipper;
    private Global os;
    private HttpReq http;
    //global varieble
    private String OTP;
    //
    private EditText phone_number_edit_text, full_name_edit_text;
    private ToggleButton password_toggle_button;
    private LinearLayout password_containner;


    ///password
    private EditText password_edit_text_1, password_edit_text_2, password_edit_text_3, password_edit_text_4, password_edit_text_5;
    private EditText otp_edit_text_1, otp_edit_text_2, otp_edit_text_3, otp_edit_text_4, otp_edit_text_5;
    //progress
    private ProgressBar otp_progress_indicator, register_progress_indicator, resend_otp_progress_indicator;

    private Button send_otp_button, register_button;
    private  TextView forgot_password_button, login_button, resend_otp_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        os = new Global(getApplicationContext());
        http = new HttpReq(getApplicationContext(), Global.Services.API);
        http.activateDebugging(true);

        OTP = com.xpeed.core.Library.generateOTP(10000,99999)+"";

        initializeComponents();
    }



    /********************************
     * Common Functions
     *******************************/
    private void attemptRegistration() {
        String mobile = getPhoneNo();
        String password = getPassword();
        String fullName = getFullName();

        HttpReq.ParamsModel model = new HttpReq.ParamsModel();
        model.add("member_register", "OK");
        model.add("username", mobile);
        model.add("password", password);
        model.add("full_name", fullName);

        http.setParams(model);

        http.onStart(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                //showProgress();
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
                        openCompleteProfileActivity();
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
                //hideProgress();
            }
        });
        http.prepare();
        http.commit(RequestTags.MEMBER_REGISTER_REQUEST);
    }
    private void sendOTP(final boolean resend){
        OTP = com.xpeed.core.Library.generateOTP(10000,99999)+"";
        os.toast(OTP);
        HttpReq.ParamsModel model = new HttpReq.ParamsModel();
        model.add("send_otp_for_register", "OK");
        model.add("mobile", getPhoneNo());
        model.add("otp", OTP);

        http.setParams(model);

        http.onStart(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                showOTPProgress();
                resend_otp_progress_indicator.setVisibility(View.VISIBLE);
            }
        });
        http.onSuccess(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                try {
                    JSONObject data = new JSONObject(getData());
                    if(!data.getBoolean("error")){
                        if(!resend){
                            main_view_flipper.showNext();
                        }
                    }
                    os.toast(data.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        http.onEnd(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                hideOTPProgress();
                resend_otp_progress_indicator.setVisibility(View.GONE);
            }
        });

        http.onError(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                os.toast(getData());
            }
        });
        http.prepare();
        http.commit(RequestTags.MEMBER_SEND_OTP_REQUEST);

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
    private String getOTP(){
        String pass = "";
        pass+=otp_edit_text_1.getText().toString();
        pass+=otp_edit_text_2.getText().toString();
        pass+=otp_edit_text_3.getText().toString();
        pass+=otp_edit_text_4.getText().toString();
        pass+=otp_edit_text_5.getText().toString();

        return pass;
    }
    private String getPhoneNo(){
        return phone_number_edit_text.getText().toString();
    }
    private String getFullName(){
        return full_name_edit_text.getText().toString();
    }
    /*************************
     * Initializes
     ****************/
    private void initializeComponents(){
        main_view_flipper = findViewById(R.id.main_view_flipper);
        phone_number_edit_text = (EditText) findViewById(R.id.phone_number_edit_text);
        full_name_edit_text = (EditText) findViewById(R.id.full_name_edit_text);
        password_containner = findViewById(R.id.password_containner);
        password_edit_text_1 = password_containner.findViewById(R.id.password_edit_text_1);
        password_edit_text_2 = password_containner.findViewById(R.id.password_edit_text_2);
        password_edit_text_3 = password_containner.findViewById(R.id.password_edit_text_3);
        password_edit_text_4 = password_containner.findViewById(R.id.password_edit_text_4);
        password_edit_text_5 = password_containner.findViewById(R.id.password_edit_text_5);

        otp_edit_text_1 = findViewById(R.id.otp_edit_text_1);
        otp_edit_text_2 = findViewById(R.id.otp_edit_text_2);
        otp_edit_text_3 = findViewById(R.id.otp_edit_text_3);
        otp_edit_text_4 = findViewById(R.id.otp_edit_text_4);
        otp_edit_text_5 = findViewById(R.id.otp_edit_text_5);

        password_toggle_button = findViewById(R.id.password_toggle_button);

        send_otp_button = (Button) findViewById(R.id.send_otp_button);
        forgot_password_button = (TextView) findViewById(R.id.forgot_password_button);
        login_button = (TextView) findViewById(R.id.login_button);
        register_button = (Button) findViewById(R.id.register_button);
        resend_otp_button = (TextView) findViewById(R.id.resend_otp_button);
        resend_otp_progress_indicator = (ProgressBar) findViewById(R.id.resend_otp_progress_indicator);

        otp_progress_indicator = findViewById(R.id.otp_progress_indicator);
        register_progress_indicator = findViewById(R.id.register_progress_indicator);

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

        otp_edit_text_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(otp_edit_text_1.getText().length()>0) {
                    otp_edit_text_2.requestFocus();
                }
            }
        });
        otp_edit_text_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(otp_edit_text_2.getText().length()>0) {
                    otp_edit_text_3.requestFocus();
                }
            }
        });
        otp_edit_text_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(otp_edit_text_3.getText().length()>0) {
                    otp_edit_text_4.requestFocus();
                }
            }
        });
        otp_edit_text_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(otp_edit_text_4.getText().length()>0) {
                    otp_edit_text_5.requestFocus();
                }
            }
        });
        otp_edit_text_5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(otp_edit_text_5.getText().length()>0) {
                    otp_edit_text_5.clearFocus();
                }
            }
        });

        /***********************
         * Backspace Event
         ***********************/
        password_edit_text_2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_1.requestFocus();
                    }
                }
                return false;
            }
        });
        password_edit_text_3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_2.requestFocus();
                    }
                }
                return false;
            }
        });
        password_edit_text_4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_3.requestFocus();
                    }
                }
                return false;
            }
        });
        password_edit_text_5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        password_edit_text_4.requestFocus();
                    }
                }
                return false;
            }
        });


        otp_edit_text_2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        otp_edit_text_1.requestFocus();
                    }
                }
                return false;
            }
        });
        otp_edit_text_3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        otp_edit_text_2.requestFocus();
                    }
                }
                return false;
            }
        });
        otp_edit_text_4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        otp_edit_text_3.requestFocus();
                    }
                }
                return false;
            }
        });
        otp_edit_text_5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if(((EditText)v).getText().length()==0){
                        otp_edit_text_4.requestFocus();
                    }
                }
                return false;
            }
        });

        /**********************
         * password toggle
         **********************/


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
        send_otp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(phone_number_edit_text.getText().length()==10) {
                    sendOTP(false);
                } else {
                    phone_number_edit_text.setError("Please input 10 digit phone number");
                }
            }
        });
        resend_otp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phone_number_edit_text.getText().length()==10) {
                    sendOTP(true);
                } else {
                    phone_number_edit_text.setError("Please input 10 digit phone number");
                }
            }
        });
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                os.toast(OTP+", Entered : "+getOTP());

                if(getOTP().matches(OTP)){
                    attemptRegistration();

                } else {
                    os.toast("You have entered wrong otp");
                }
            }
        });
    }
    /**************************
     * Intent methods
     **********************/
    private void openMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
    private void openLoginActivity(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
    private void openCompleteProfileActivity(){
        Intent i = new Intent(this, CompleteProfileActivity.class);
        startActivity(i);
        finish();
    }

    /********************************
     * Request Tags
     *******************************/
    private static class RequestTags {
        static final String MEMBER_REGISTER_REQUEST="RequestTags:MEMBER_REGISTER_REQUEST";
        static final String MEMBER_SEND_OTP_REQUEST="RequestTags:MEMBER_SEND_OTP_REQUEST";
    }

    private void showOTPProgress() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                send_otp_button.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        otp_progress_indicator.startAnimation(anim);
    }

    private void hideOTPProgress(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                send_otp_button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        otp_progress_indicator.startAnimation(anim);
    }

    @Override
    public void onBackPressed() {
        openLoginActivity();
        super.onBackPressed();
    }
}
