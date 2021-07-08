package com.codeplay.scanner_demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentEventListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container,
                        BarcodeCaptureFragment.newInstance(R.string.app_name, null))
                .commit();
    }

    @Override
    public void setFragment(BaseFragment fragment) {

    }

    @Override
    public void onResume(int index) {

    }
}
