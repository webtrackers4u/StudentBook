package net.reminderbook.studentbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CompleteProfileActivity extends AppCompatActivity {
    private Global os;
    private JSONArray boards;
    private JSONArray classes;


    //containner
    private ViewFlipper viewFlipper;
    private RelativeLayout grade_containner;
    private RelativeLayout profile_containner;
    ///
    private String boardSelected;
    private String classSelected;
    private String selectedEmail;
    private String selectedAddress;
    private String selectedType;

    private Spinner board_spinner, class_spinner;
    //profile
    RadioGroup user_type_RadioGroup;
    EditText email_EditText, address_EditText;
    Button save_profile_button;

    ProgressBar progress_indicator;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        os = new Global(getApplicationContext());

        //containner
        viewFlipper = findViewById(R.id.viewFlipper);
        viewFlipper.setInAnimation(this,R.anim.zoom_in);
        viewFlipper.setOutAnimation(this, R.anim.zoom_out);

        board_spinner = findViewById(R.id.board_spinner);
        class_spinner = findViewById(R.id.class_spinner);
        //profile page
        user_type_RadioGroup = findViewById(R.id.user_type_RadioGroup);
        email_EditText = findViewById(R.id.email_EditText);
        address_EditText = findViewById(R.id.address_EditText);

        progress_indicator = findViewById(R.id.progress_indicator);
        save_profile_button = findViewById(R.id.save_profile_button);
        save_profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                save_profile();
            }
        });

        user_type_RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( RadioGroup group, int checkedId ) {
                switch (checkedId){
                    case R.id.student_type:
                        selectedType = "student";
                        break;
                    case R.id.guardians_type:
                        selectedType = "guardians";
                        break;
                    case R.id.teacher_type:
                        selectedType = "teacher";
                        break;
                }
            }
        });

        switch (user_type_RadioGroup.getCheckedRadioButtonId()){
            case R.id.student_type:
                selectedType = "student";
                break;
            case R.id.guardians_type:
                selectedType = "guardians";
                break;
            case R.id.teacher_type:
                selectedType = "teacher";
                break;
        }


        email_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
                email_EditText.setError("");
            }
            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) { }
            @Override
            public void afterTextChanged( Editable s ) { }
        });
        address_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
                address_EditText.setError("");
            }
            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) { }
            @Override
            public void afterTextChanged( Editable s ) { }
        });

        final HttpReq http = new HttpReq(getApplicationContext(),Global.Services.API);

        HttpReq.ParamsModel model = new HttpReq.ParamsModel();
        model.add("fetch_grades","OK");
        model.add("subjectId","1");
        http.setParams(model);


        http.onSuccess(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                String response = getData();
                try {
                    JSONObject data = new JSONObject(response);
                    if(data.getBoolean("error")){
                        os.toast(data.getString("message"));
                    } else {
                        boards = data.getJSONArray("board");
                        classes = data.getJSONArray("class");
                        loadBoard();
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        http.onEnd(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                os.toast("This is the end");
            }
        });

        http.prepare();
        http.commit(this);


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);


        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

    }

    public static final int PICK_IMAGE = 1;

    private void  loadBoard(){
        ArrayList<String> boardsArray  = new ArrayList<String>();
        final ArrayList<String> boardIdsArray = new ArrayList<String>();
        for(int a=0; a<boards.length(); a++){
           try {
               JSONObject board = boards.getJSONObject(a);
               boardsArray.add(board.getString("name"));
               boardIdsArray.add(board.getString("boardId"));
           } catch (JSONException e) {
               e.printStackTrace();
           }

           ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_selected_text, boardsArray);
           adapter.setDropDownViewResource(R.layout.spinner_item_layout);
           board_spinner.setAdapter(adapter);
       }

        board_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                boardSelected = boardIdsArray.get(position);
                loadClass();
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {

            }
        });
    }
    ///load Class
    private void loadClass(){
        final ArrayList<String> classesArray  = new ArrayList<String>();
        final ArrayList<String> classIdsArray = new ArrayList<>();
        for(int a=0; a<classes.length(); a++) {
            try {
                final JSONObject classJson = classes.getJSONObject(a);
                String boardId = classJson.getString("boardId");
                if(boardId.matches(boardSelected)) {
                    classesArray.add(classJson.getString("name"));
                    classIdsArray.add(classJson.getString("classId"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_selected_text, classesArray);
            adapter.setDropDownViewResource(R.layout.spinner_item_layout);
            class_spinner.setAdapter(adapter);
        }
        class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                classSelected = classIdsArray.get(position);
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {

            }
        });

    }
    void save_profile(){
        if(address_EditText.getText().toString().isEmpty()){
            address_EditText.setError("Please enter address");
        }
        if(!address_EditText.getText().toString().isEmpty()){

            selectedEmail = email_EditText.getText().toString();
            selectedAddress = address_EditText.getText().toString();

            HttpReq http = new HttpReq(getApplicationContext(), Global.Services.API);

            HttpReq.ParamsModel model = new HttpReq.ParamsModel();
            model.add("member_update_details","OK");
            model.add("name","");
            model.add("email",selectedEmail);
            model.add("address",selectedAddress);
            model.add("type",selectedType);
            model.add("memberId",os.get_logged_user());
            model.add("boardId", boardSelected);
            model.add("classId",classSelected);
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
                            os.fetch_startup_data(new Runnable() {
                                @Override
                                public void run() {
                                    openMainActivity();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    os.toast(getData());
                }
            });

            http.onEnd(new HttpReq.CallbackListener(){
                @Override
                public void run() {
                    super.run();
                    hideProgress();
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
            http.commit(this);
        }

    }

    /*******************************
     * Intent Activities
     ******************************/
    private void openMainActivity(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showProgress() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                save_profile_button.setVisibility(View.GONE);
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

    private void hideProgress(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                save_profile_button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progress_indicator.startAnimation(anim);
    }
}
