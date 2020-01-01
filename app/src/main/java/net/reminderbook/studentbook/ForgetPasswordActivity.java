package net.reminderbook.studentbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class ForgetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
    }

    @Override
    public void onBackPressed() {
        openLoginActivity();
        super.onBackPressed();
    }

    /**************************
     * Intent methods
     **********************/
    private void openLoginActivity(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
