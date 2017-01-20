package com.iplant.view.activity;

import android.app.Activity;
import android.os.Bundle;

import static android.R.style.Theme;

public class SelfCloseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(android.R.style.Theme_NoDisplay);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
