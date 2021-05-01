package com.fenchtose.tooltip_demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FragmentHolderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);

        Fragment fragment = new TooltipFragmentDemo();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit();
    }
}
