package com.krikki.vocabularytrainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.krikki.vocabularytrainer.dictionary.Dictionary;
import com.krikki.vocabularytrainer.games.GameMenu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Controls main menu activity.
 */
public class MainMenu extends AppCompatActivity {
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;

    private Button buttonGames, buttonDict, buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_main_menu);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }



        buttonGames = findViewById(R.id.button_games);
        buttonDict = findViewById(R.id.button_dict);
        buttonSettings = findViewById(R.id.button_settings);

        buttonGames.setOnClickListener(view -> {
            Intent intent = new Intent(this, GameMenu.class);
            startActivity(intent);
        });
        buttonDict.setOnClickListener(view -> {
            Intent intent = new Intent(this, Dictionary.class);
            startActivity(intent);
        });
        buttonSettings.setOnClickListener(view -> {
            Toast.makeText(this, "You have no permission to edit settings", Toast.LENGTH_SHORT).show();
        });
    }
}
