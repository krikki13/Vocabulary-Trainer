package com.krikki.vocabularytrainer.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.games.quiz.QuizActivity;

import java.util.function.BiConsumer;

import androidx.appcompat.app.AppCompatActivity;

import static com.krikki.vocabularytrainer.games.quiz.QuizGenerator.QuizType;

/**
 * Controls game menu activity.
 */
public class GameMenu extends AppCompatActivity {
    private Button buttonPrimaryTranslated, buttonPrimaryDesc, buttonTranslatedPrimary, buttonDescPrimary;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_game_menu);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        buttonPrimaryTranslated = findViewById(R.id.button_primary_translated);
        buttonPrimaryDesc = findViewById(R.id.button_primary_desc);
        buttonTranslatedPrimary = findViewById(R.id.button_translated_primary);
        buttonDescPrimary = findViewById(R.id.button_desc_primary);

        BiConsumer<QuizType, QuizType> quizLauncher = (quizQuestionType, quizAnswerType) -> {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("quizQuestionType", quizQuestionType.toString());
            intent.putExtra("quizAnswerType", quizAnswerType.toString());
            startActivity(intent);
        };
        buttonPrimaryTranslated.setOnClickListener(view -> quizLauncher.accept(QuizType.PRIMARY_LANG, QuizType.SECONDARY_LANG));
        buttonPrimaryDesc.setOnClickListener(view -> quizLauncher.accept(QuizType.PRIMARY_LANG, QuizType.DESCRIPTION));
        buttonTranslatedPrimary.setOnClickListener(view -> quizLauncher.accept(QuizType.SECONDARY_LANG, QuizType.PRIMARY_LANG));
        buttonDescPrimary.setOnClickListener(view -> quizLauncher.accept(QuizType.DESCRIPTION, QuizType.PRIMARY_LANG));
    }
}
