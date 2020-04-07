package com.krikki.vocabularytrainer.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.games.CommonGameGenerator.GameType;
import com.krikki.vocabularytrainer.games.quiz.QuizActivity;
import com.krikki.vocabularytrainer.games.write.WriteActivity;
import com.krikki.vocabularytrainer.util.TriConsumer;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Controls game menu activity.
 */
public class GameMenu extends AppCompatActivity {
    private Button quizButtonPrimaryTranslated, quizButtonPrimaryDesc, quizButtonTranslatedPrimary, quizButtonDescPrimary;
    private Button writeButtonPrimaryTranslated, writeButtonTranslatedPrimary, writeButtonDescPrimary;
    private Button hangmanButtonPlay;
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

        writeButtonPrimaryTranslated = findViewById(R.id.write_button_primary_translated);
        writeButtonTranslatedPrimary = findViewById(R.id.write_button_translated_primary);
        writeButtonDescPrimary = findViewById(R.id.write_button_desc_primary);

        hangmanButtonPlay = findViewById(R.id.hangman_play);

        TriConsumer<Class, GameType, GameType> gameLauncher = (activityClass, quizQuestionType, quizAnswerType) -> {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("gameQuestionType", quizQuestionType.toString());
            intent.putExtra("gameAnswerType", quizAnswerType.toString());
            startActivity(intent);
        };
        quizButtonPrimaryTranslated.setOnClickListener(view -> gameLauncher.accept(QuizActivity.class, GameType.PRIMARY_LANG, GameType.SECONDARY_LANG));
        quizButtonPrimaryDesc.setOnClickListener(view -> gameLauncher.accept(QuizActivity.class, GameType.PRIMARY_LANG, GameType.DESCRIPTION));
        quizButtonTranslatedPrimary.setOnClickListener(view -> gameLauncher.accept(QuizActivity.class, GameType.SECONDARY_LANG, GameType.PRIMARY_LANG));
        quizButtonDescPrimary.setOnClickListener(view -> gameLauncher.accept(QuizActivity.class, GameType.DESCRIPTION, GameType.PRIMARY_LANG));

        writeButtonPrimaryTranslated.setOnClickListener(view -> gameLauncher.accept(WriteActivity.class, GameType.PRIMARY_LANG, GameType.SECONDARY_LANG));
        writeButtonTranslatedPrimary.setOnClickListener(view -> gameLauncher.accept(WriteActivity.class, GameType.SECONDARY_LANG, GameType.PRIMARY_LANG));
        writeButtonDescPrimary.setOnClickListener(view -> gameLauncher.accept(WriteActivity.class, GameType.DESCRIPTION, GameType.PRIMARY_LANG));

        hangmanButtonPlay.setOnClickListener(view -> Toast.makeText(this  , "I said it is coming soon dammit!!", Toast.LENGTH_SHORT).show());
    }
}
