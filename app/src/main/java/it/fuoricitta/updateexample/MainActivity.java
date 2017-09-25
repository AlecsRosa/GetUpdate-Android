package it.fuoricitta.updateexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import it.fuoricitta.update.UpdateManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // UPDATE SETUP
        UpdateManager.instance.setup(getApplicationContext(), "A4JFAUNCMBX65VR0"); // APP ID

        // UPDATE REQUEST
        UpdateManager.instance.askForUpdate(this, null);
    }
}