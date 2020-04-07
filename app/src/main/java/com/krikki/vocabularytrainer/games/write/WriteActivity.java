package com.krikki.vocabularytrainer.games.write;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.krikki.vocabularytrainer.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Main activity for write game.
 */
public class WriteActivity extends AppCompatActivity {
    private WriteGame writeGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.simple_frame_layout);

        writeGame = new WriteGame();
        loadFragment(writeGame, "writeGame");
        /*quizResults = new QuizResults();
        Bundle bundle = new Bundle();
        bundle.putInt("score", 9);
        bundle.putString("gifUrl", gifUrl);
        quizResults.setArguments(bundle);
        loadFragment(quizResults, "quizGame");*/
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
}