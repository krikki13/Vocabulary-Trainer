package com.krikki.vocabularytrainer.games.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.krikki.vocabularytrainer.R;

import androidx.fragment.app.Fragment;

public class QuizResults extends Fragment {
    private TextView congratsText, messageText;
    private Button exitButton;
    private ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_quiz_results, container, false);

        int score = -1;
        String gifUrl = "";
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            score = bundle.getInt("score", -1);
            gifUrl = bundle.getString("gifUrl", "");
        }

        congratsText = view.findViewById(R.id.congratsText);
        messageText = view.findViewById(R.id.messageText);
        exitButton = view.findViewById(R.id.exitButton);
        imageView = view.findViewById(R.id.imageView);

        if(score >= 9) {
            congratsText.setText("Congratulations");
            Glide.with(getActivity())
                    .load(gifUrl)
                    .into(imageView);
        }else if(score >= 7){
            congratsText.setText("Good job");
        }else if(score >= 5){
            congratsText.setText("Well enough");
        }else{
            congratsText.setText("You should try harder next time");
            Glide.with(getActivity())
                    .load(gifUrl)
                    .into(imageView);
        }

        messageText.setText("Your score: " + score);

        exitButton.setOnClickListener(view1 -> getActivity().finish());
        return view;
    }
}