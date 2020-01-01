package net.reminderbook.studentbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

public final class FilterActivity extends AppCompatActivity {
    /***************
     * Values
     ***************/
    int request_code = -1;
    Intent parent_intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        request_code = (getIntent().getIntExtra("request_code", -1));
        parent_intent = new Intent();
        initializeComponent();


    }

    private void initializeComponent(){
        switch (request_code) {
            case ActivityConstants.FILTER_QUESTION:
                questionInitialize();
                break;
            case ActivityConstants.FILTER_SUGGESTION:


                break;
        }

        /****************************
         * Listener
         ****************************/
        ((RadioGroup)findViewById(R.id.favourite_filter_radio_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.fav_yes:
                        parent_intent.putExtra("favourite", "Yes");
                        break;
                    case R.id.fav_no:
                        parent_intent.putExtra("favourite", "No");
                        break;
                    case R.id.fav_all:
                        parent_intent.putExtra("favourite", "");
                        break;
                }
            }
        });

        ((RadioGroup)findViewById(R.id.importance_filter_radio_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.im_all:
                        parent_intent.putExtra("importance", "");
                        break;
                    case R.id.ni:
                        parent_intent.putExtra("importance", "1");
                        break;
                    case R.id.i:
                        parent_intent.putExtra("importance", "2");
                        break;
                    case R.id.vi:
                        parent_intent.putExtra("importance", "3");
                        break;
                    case R.id.vvi:
                        parent_intent.putExtra("importance", "4");
                        break;
                    case R.id.vvvi:
                        parent_intent.putExtra("importance", "5");
                        break;
                }

            }
        });

        /////

        ((RadioGroup)findViewById(R.id.over_all_star_filter_radio_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch(radioGroup.getCheckedRadioButtonId()){
                    case R.id.os_all:
                        parent_intent.putExtra("overall_star", "");
                        break;
                    case R.id.os1:
                        parent_intent.putExtra("overall_star", "1");
                        break;
                    case R.id.os2:
                        parent_intent.putExtra("overall_star", "2");
                        break;
                    case R.id.os3:
                        parent_intent.putExtra("overall_star", "3");
                        break;
                    case R.id.os4:
                        parent_intent.putExtra("overall_star", "4");
                        break;
                    case R.id.os5:
                        parent_intent.putExtra("overall_star", "5");
                        break;
                }
            }
        });

        ((Button) findViewById(R.id.search_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
    }

    /**********************
     * For Question page
     *********************/
    private void questionInitialize(){
        parent_intent.putExtra("overall_star", "");
        parent_intent.putExtra("importance", "");
        parent_intent.putExtra("favourite", "");

        switch ((getIntent().getStringExtra("favourite"))){
            case "Yes":
                ((RadioGroup)findViewById(R.id.favourite_filter_radio_group)).check(R.id.fav_yes);
                break;
            case "No":
                ((RadioGroup)findViewById(R.id.favourite_filter_radio_group)).check(R.id.fav_no);
                break;
            case "":
                ((RadioGroup)findViewById(R.id.favourite_filter_radio_group)).check(R.id.fav_all);
                break;
        };

        switch ((getIntent().getStringExtra("importance"))){
            case "":
                ((RadioGroup)findViewById(R.id.importance_filter_radio_group)).check(R.id.im_all);
                break;
            case "1":
                ((RadioGroup)findViewById(R.id.importance_filter_radio_group)).check(R.id.ni);
                break;
            case "2":
                ((RadioGroup)findViewById(R.id.importance_filter_radio_group)).check(R.id.i);
                break;
            case "3":
                ((RadioGroup)findViewById(R.id.importance_filter_radio_group)).check(R.id.vi);
                break;
            case "4":
                ((RadioGroup)findViewById(R.id.importance_filter_radio_group)).check(R.id.vvi);
                break;
            case "5":
                ((RadioGroup)findViewById(R.id.importance_filter_radio_group)).check(R.id.vvvi);
                break;
        };

        switch ((getIntent().getStringExtra("overall_star"))){
            case "":
                ((RadioGroup)findViewById(R.id.over_all_star_filter_radio_group)).check(R.id.os_all);
                break;
            case "1":
                ((RadioGroup)findViewById(R.id.over_all_star_filter_radio_group)).check(R.id.os1);
                break;
            case "2":
                ((RadioGroup)findViewById(R.id.over_all_star_filter_radio_group)).check(R.id.os2);
                break;
            case "3":
                ((RadioGroup)findViewById(R.id.over_all_star_filter_radio_group)).check(R.id.os3);
                break;
            case "4":
                ((RadioGroup)findViewById(R.id.over_all_star_filter_radio_group)).check(R.id.os4);
                break;
            case "5":
                ((RadioGroup)findViewById(R.id.over_all_star_filter_radio_group)).check(R.id.os5);
                break;
        }
    }
    private void search(){
        setResult(request_code, parent_intent);
        finish();
    }

    public interface ActivityConstants {
        public static final int FILTER_QUESTION     = 1001;
        public static final int FILTER_SUGGESTION   = 1003;
    }
}


