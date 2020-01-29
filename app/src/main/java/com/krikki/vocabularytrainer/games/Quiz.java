package com.krikki.vocabularytrainer.games;

import android.graphics.Typeface;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.Random;
import java.util.TreeSet;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Controls quiz activity.
 */
public class Quiz extends AppCompatActivity {
    private TextView question;
    private Button[] buttonAnswers;
    private Button buttonNext;
    private ArrayList<Word> words;
    private ArrayList<Word> questions;

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

    /**
     * Selects 10 words from word list as questions and adds them to questions field. Words are picked
     * according to their score. List is divided to 10 sections which are valued by average score of
     * contained words. This represents the probability for words to be selected from that section.
     * At least 30 words are required, otherwise activity is finished.
     */
    private void pickQuestions(){
        if(words.size() < 30){
            Toast.makeText(this, "There are too few words to generate a quiz. Have at least 30 words", Toast.LENGTH_LONG).show();
            finish();
        }
        final ArrayList<Word> list = new ArrayList<>(words);
        list.sort(Word.comparatorByScore());

        // find distribution of scores in words (result is array of avg values for each tenth of the word score list)
        // length 10
        final int[] distribution = new int[10];
        int i = 0;
        int dec = 1;
        final int size = list.size();
        int sum = 0;
        int count = 0;
        for(Word word : list){
            if(i == dec*size/10){
                distribution[dec-1] = sum/count;
                dec++;
                sum = 0;
                count = 0;
            }
            count++;
            sum += word.getScore();
            i++;
        }
        distribution[9] = sum/count;

        // generate probability arraylist where values represent tenths of word array from which question will be picked
        // length >10, values: 0-9
        final ArrayList<Integer> probabilities = new ArrayList<>();
        final int divideBy = Word.MAX_TOTAL_SCORE / 10;
        for (int j = 0; j < distribution.length; j++) {
            int numOfCopies = (Word.MAX_TOTAL_SCORE - distribution[j])/divideBy;
            numOfCopies = Math.max(numOfCopies, 1);
            probabilities.addAll(Collections.nCopies(numOfCopies, j));
        }

        final Random random = new Random();
        final TreeSet<Integer> wordNumbers = new TreeSet<>();
        final int tenthLength = list.size()/10;
        for (int k = 0; k < 10; k++) {
            int tenth = probabilities.get(random.nextInt(probabilities.size()));
            int wordInTenth = random.nextInt(tenthLength);
            if(!wordNumbers.add(tenth * tenthLength + wordInTenth)){
                k--;
            }
        }

        wordNumbers.forEach(wordNumber -> questions.add(words.get(wordNumber)));
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
