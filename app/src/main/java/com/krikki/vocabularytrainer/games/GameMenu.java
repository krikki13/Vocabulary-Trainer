package com.krikki.vocabularytrainer.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.games.quiz.QuizActivity;

import java.util.function.BiConsumer;

import androidx.appcompat.app.AppCompatActivity;

import static com.krikki.vocabularytrainer.games.quiz.QuizGenerator.QuizType;

/**
 * Controls game menu activity.
 */
public class GameMenu extends AppCompatActivity {
    private Button quizButtonPrimaryTranslated, quizButtonPrimaryDesc, quizButtonTranslatedPrimary, quizButtonDescPrimary;
    private Button hangmanButtonPlay, writeButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_game_menu);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        quizButtonPrimaryTranslated = findViewById(R.id.quiz_button_primary_translated);
        quizButtonPrimaryDesc = findViewById(R.id.quiz_button_primary_desc);
        quizButtonTranslatedPrimary = findViewById(R.id.quiz_button_translated_primary);
        quizButtonDescPrimary = findViewById(R.id.quiz_button_desc_primary);

        hangmanButtonPlay = findViewById(R.id.hangman_play);
        writeButton = findViewById(R.id.write_play);

        BiConsumer<QuizType, QuizType> quizLauncher = (quizQuestionType, quizAnswerType) -> {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("quizQuestionType", quizQuestionType.toString());
            intent.putExtra("quizAnswerType", quizAnswerType.toString());
            startActivity(intent);
        };
        quizButtonPrimaryTranslated.setOnClickListener(view -> quizLauncher.accept(QuizType.PRIMARY_LANG, QuizType.SECONDARY_LANG));
        quizButtonPrimaryDesc.setOnClickListener(view -> quizLauncher.accept(QuizType.PRIMARY_LANG, QuizType.DESCRIPTION));
        quizButtonTranslatedPrimary.setOnClickListener(view -> quizLauncher.accept(QuizType.SECONDARY_LANG, QuizType.PRIMARY_LANG));
        quizButtonDescPrimary.setOnClickListener(view -> quizLauncher.accept(QuizType.DESCRIPTION, QuizType.PRIMARY_LANG));

        hangmanButtonPlay.setOnClickListener(view -> Toast.makeText(this  , "I said it is coming soon dammit!!", Toast.LENGTH_SHORT).show());
        writeButton.setOnClickListener(view -> Toast.makeText(this, "I said it is coming soon dammit!!", Toast.LENGTH_SHORT).show());
    }
}
