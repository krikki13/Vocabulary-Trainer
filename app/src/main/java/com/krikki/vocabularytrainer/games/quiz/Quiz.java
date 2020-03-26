package com.krikki.vocabularytrainer.games.quiz;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.krikki.vocabularytrainer.DataStorageManager;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static com.krikki.vocabularytrainer.games.quiz.QuizGenerator.QuizType;

/**
 * Controls quiz activity.
 */
public class Quiz extends AppCompatActivity {
    private TextView question;
    private List<Button> buttonAnswers;
    private Button buttonNext;
    private ArrayList<Word> words;
    private QuizGenerator quizGenerator;
    private int numOfCorrectAnswers = 0;
    private boolean buttonsDisabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_quiz);

        question = findViewById(R.id.tvQuestion);
        buttonAnswers = new ArrayList<>();
        buttonAnswers.add(findViewById(R.id.bAnswer1));
        buttonAnswers.add(findViewById(R.id.bAnswer2));
        buttonAnswers.add(findViewById(R.id.bAnswer3));
        buttonAnswers.add(findViewById(R.id.bAnswer4));
        buttonNext = findViewById(R.id.buttonNext);

        buttonAnswers.get(0).setOnClickListener(view -> onAnswerClick(0));
        buttonAnswers.get(1).setOnClickListener(view -> onAnswerClick(1));
        buttonAnswers.get(2).setOnClickListener(view -> onAnswerClick(2));
        buttonAnswers.get(3).setOnClickListener(view -> onAnswerClick(3));
        buttonNext.setOnClickListener(view -> {
            if(quizGenerator.hasNext()) {
                buttonsDisabled = false;
                buttonAnswers.forEach(button -> setButtonBackgroundColor(button, R.color.defaultButtonBackgroundColor));
                buttonNext.setVisibility(View.GONE);
                quizGenerator.next();
                showQuestion();
            }else{
                Toast.makeText(Quiz.this, "You scored "+numOfCorrectAnswers, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        readListOfWordsFromStorage();
        try {
            Intent intent = getIntent();
            QuizType questionType = QuizType.valueOf(intent.getStringExtra("quizQuestionType"));
            QuizType answerType = QuizType.valueOf(intent.getStringExtra("quizAnswerType"));
            quizGenerator = new QuizGenerator(words, questionType, answerType);
            showQuestion();
        } catch (QuizGenerationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showQuestion(){
        question.setText(quizGenerator.getQuestion());
        final List<String> answers = quizGenerator.getAllAnswers();
        for (int i = 0; i < 4; i++) {
            buttonAnswers.get(i).setText(answers.get(i));
        }
    }

    private void setButtonBackgroundColor(Button button, int color){
        Drawable background = button.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable)background).getPaint().setColor(ContextCompat.getColor(this, color));
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable)background).setColor(ContextCompat.getColor(this, color));
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable)background).setColor(ContextCompat.getColor(this, color));
        }
    }

    private void onAnswerClick(int buttonIndex){
        if(!buttonsDisabled){
            if (buttonIndex == quizGenerator.getCorrectAnswerIndex()) {
                setButtonBackgroundColor(buttonAnswers.get(buttonIndex), R.color.correctBackgroundColor);
                numOfCorrectAnswers++;
            } else {
                setButtonBackgroundColor(buttonAnswers.get(buttonIndex), R.color.incorrectBackgroundColor);
                setButtonBackgroundColor(buttonAnswers.get(quizGenerator.getCorrectAnswerIndex()), R.color.correctBackgroundColor);
            }
            buttonNext.setVisibility(View.VISIBLE);
            buttonsDisabled = true;

            int i = 0;
            String delimiter = " = ";
            if(quizGenerator.getQuestionType() == QuizType.DESCRIPTION || quizGenerator.getAnswerType() == QuizType.DESCRIPTION){
                delimiter = "\n=\n";
            }
            for (String correctTranslationForAnswer : quizGenerator.getAnswersTranslated()) {
                buttonAnswers.get(i++).append(delimiter + correctTranslationForAnswer);
            }
        }
    }


    /**
     * Read words from storage and put them in List. If any exception occurs when reading or parsing,
     * Toast error message is displayed and activity finishes.
     */
    private void readListOfWordsFromStorage(){
        DataStorageManager storageManager = new DataStorageManager(getApplicationContext());
        try {
            String wordsText = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            words = storageManager.convertToListOfWords(wordsText);
        } catch (IOException | JSONException e) {
            Toast.makeText(this, "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        } catch (Word.DuplicatedIdException | Word.UnsuccessfulWordCreationException e1){
            Toast.makeText(this, "Data file is incorrectly formatted", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
