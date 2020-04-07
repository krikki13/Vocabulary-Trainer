package com.krikki.vocabularytrainer.games.quiz;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.krikki.vocabularytrainer.R;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Main activity for quiz game. When created it loads {@link QuizGame} that controls the actual game.
 * When QuizGame finishes, {@link QuizResults} is loaded.
 */
public class QuizActivity extends AppCompatActivity implements QuizGame.QuizEventListener, QuizResults.DataCommunicator {
    private QuizGame quizGame;
    private QuizResults quizResults;
    private GiphyConnector giphyConnector;
    private String gifUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.simple_frame_layout);

        quizGame = new QuizGame();
        loadFragment(quizGame, "quizGame");
    }

    private void loadFragment(Fragment frag, String tag){
        // Create new fragment and transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.fragmentContainer, frag, tag);
        //transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void quizNearlyFinished(int intermediateScore){
        if(intermediateScore >= 8 || intermediateScore < 5){
            giphyConnector = new GiphyConnector(QuizActivity.this);
            giphyConnector.setOnResponse((status, message) -> {
                if(status == 200){
                    gifUrl = message;
                }
            });
            giphyConnector.getRandomGif(intermediateScore >= 8 ? "well done" : "sad");
        }
    }

    @Override
    public void quizFinished(int score, List<QuizGenerator.QuestionWord> mistakesList) {
        // TODO
        quizResults = new QuizResults();
        Bundle bundle = new Bundle();
        bundle.putInt("score", score);
        bundle.putString("gifUrl", gifUrl);
        quizResults.setArguments(bundle);
        loadFragment(quizResults, "quizResults");
    }

    @Override
    public List<QuizGenerator.QuestionWord> obtainMistakesList() {
        return quizGame.getMistakesList();
    }
}
