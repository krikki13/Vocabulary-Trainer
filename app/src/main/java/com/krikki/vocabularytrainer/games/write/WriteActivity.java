package com.krikki.vocabularytrainer.games.write;

import android.os.Bundle;
import android.widget.Toast;

import com.krikki.vocabularytrainer.DataStorageManager;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.util.SoftInputAdjustAssist;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Main activity for write game.
 */
public class WriteActivity extends AppCompatActivity implements WriteGame.GameControlActivity, WriteGameResults.DataCommunicator {
    private DataStorageManager storageManager;
    private WriteGame writeGame;
    private WriteGameResults writeGameResults;
    private List<Word> words;
    private List<WriteGame.QuestionAnswerObject> questionsAndAnswers;

    private SoftInputAdjustAssist softInputAdjustAssist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_frame_layout);
        softInputAdjustAssist = new SoftInputAdjustAssist(this);

        storageManager = new DataStorageManager(this);
        readWordsFromStorage();

        writeGame = new WriteGame();
        loadFragment(writeGame, "writeGame");
    }

    private void loadFragment(Fragment frag, String tag) {
        // Create new fragment and transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.fragmentContainer, frag, tag);
        //transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    /**
     * Read words from storage and put them in List. If any exception occurs when reading or parsing,
     * Toast error message is displayed and activity finishes.
     */
    private void readWordsFromStorage(){
        try {
            String wordsText = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            words = storageManager.convertToListOfWords(wordsText);
        } catch (IOException | JSONException e) {
            Toast.makeText(this, "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
            this.finish();
        } catch (Word.DuplicatedIdException | Word.UnsuccessfulWordCreationException e1){
            Toast.makeText(this, "Data file is incorrectly formatted. Please go to dictionary for more options", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    @Override
    public List<Word> getWordList() {
        return words;
    }

    @Override
    public void onGameFinished(List<WriteGame.QuestionAnswerObject> questionsAndAnswers) {
        this.questionsAndAnswers = questionsAndAnswers;
        writeGameResults = new WriteGameResults();
        loadFragment(writeGameResults, "writeGameResults");
    }

    @Override
    public List<WriteGame.QuestionAnswerObject> obtainQuestionsAnswersList() {
        return questionsAndAnswers;
    }

    @Override
    public List<Word> obtainWords() {
        return words;
    }

    @Override
    protected void onResume() {
        softInputAdjustAssist.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        softInputAdjustAssist.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        softInputAdjustAssist.onDestroy();
        super.onDestroy();
    }
}