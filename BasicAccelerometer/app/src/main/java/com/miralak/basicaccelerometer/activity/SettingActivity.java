package com.miralak.basicaccelerometer.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.miralak.basicaccelerometer.R;
import com.miralak.basicaccelerometer.SettingsKey;

public class SettingActivity extends ActionBarActivity {
    private EditText restUri;
    private EditText userID;
    private EditText acquisitionTime;
    private Button saveBtn;

    private SharedPreferences sharedPreferences;

    private final View.OnClickListener onBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SettingsKey.userID, userID.getText().toString());
            editor.putString(SettingsKey.restUri, restUri.getText().toString());
            editor.putInt(SettingsKey.acquisitionTime, Integer.parseInt(acquisitionTime.getText().toString()));
            editor.putBoolean(SettingsKey.firstUse, false);

            editor.commit();

            finish();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        restUri = (EditText) findViewById(R.id.restUri);
        userID = (EditText) findViewById(R.id.userID);
        acquisitionTime = (EditText) findViewById(R.id.acquisitionTime);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(onBtnClick);

        sharedPreferences = getSharedPreferences(SettingsKey.appSetting, MODE_PRIVATE);
        loadPref();
    }

    private void loadPref() {
        acquisitionTime.setText(String.valueOf(sharedPreferences.getInt(SettingsKey.acquisitionTime, 20000)));
        userID.setText(sharedPreferences.getString(SettingsKey.userID, "Test User"));
        restUri.setText(sharedPreferences.getString(SettingsKey.restUri, "your's address"));
    }
}
