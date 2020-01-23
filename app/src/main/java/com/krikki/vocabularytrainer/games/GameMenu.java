package com.krikki.vocabularytrainer.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.krikki.vocabularytrainer.R;

import java.util.function.Consumer;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Controls game menu activity.
 */
public class GameMenu extends AppCompatActivity {
    private Button buttonPrimaryTranslated, buttonPrimaryDesc, buttonTranslatedPrimary, buttonDescPrimary;
    public enum QuizType{
        PRIMARY_TRANSLATED,
        PRIMARY_DESC,
        TRANSLATED_PRIMARY,
        DESC_PRIMARY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_game_menu);

        buttonPrimaryTranslated = findViewById(R.id.button_primary_translated);
        buttonPrimaryDesc = findViewById(R.id.button_primary_desc);
        buttonTranslatedPrimary = findViewById(R.id.button_translated_primary);
        buttonDescPrimary = findViewById(R.id.button_desc_primary);

        Consumer<QuizType> quizLauncher = quizType -> {
            Intent intent = new Intent(this, Quiz.class);
            intent.putExtra("quizType", quizType);
            startActivity(intent);
        };
        buttonPrimaryTranslated.setOnClickListener(view -> quizLauncher.accept(QuizType.PRIMARY_TRANSLATED));
        buttonPrimaryDesc.setOnClickListener(view -> quizLauncher.accept(QuizType.PRIMARY_DESC));
        buttonTranslatedPrimary.setOnClickListener(view -> quizLauncher.accept(QuizType.TRANSLATED_PRIMARY));
        buttonDescPrimary.setOnClickListener(view -> quizLauncher.accept(QuizType.DESC_PRIMARY));
    }
}
