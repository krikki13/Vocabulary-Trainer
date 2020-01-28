package com.krikki.vocabularytrainer.games;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Controls quiz activity.
 */
public class Quiz extends AppCompatActivity {
    private TextView question;
    private Button[] buttonAnswers;
    private Button buttonNext;
    private ArrayList<Word> words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_quiz);

        question = findViewById(R.id.tvQuestion);
        buttonAnswers = new Button[4];
        buttonAnswers[0] = findViewById(R.id.bAnswer1);
        buttonAnswers[1] = findViewById(R.id.bAnswer2);
        buttonAnswers[2] = findViewById(R.id.bAnswer3);
        buttonAnswers[3] = findViewById(R.id.bAnswer4);
        buttonNext = findViewById(R.id.buttonNext);

        question.setText("Shake slightly and uncontrollably as a result of being cold, frightened, or excited");
        buttonAnswers[0].setText("revenant");
        buttonAnswers[1].setText("perseverance");
        buttonAnswers[2].setText("shudder");
        buttonAnswers[3].setText("insurgence");

        buttonAnswers[0].setOnClickListener(view -> {
            question.setTypeface(null, Typeface.BOLD);
        });
        buttonAnswers[1].setOnClickListener(view -> {
            question.setTypeface(null, Typeface.ITALIC);
        });
        buttonAnswers[2].setOnClickListener(view -> {
            question.setTypeface(null, Typeface.BOLD_ITALIC);
        });
        buttonAnswers[3].setOnClickListener(view -> {
            question.setTypeface(null, Typeface.NORMAL);
        });

    }
}
