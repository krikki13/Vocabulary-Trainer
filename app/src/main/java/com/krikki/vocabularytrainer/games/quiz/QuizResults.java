package com.krikki.vocabularytrainer.games.quiz;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.krikki.vocabularytrainer.R;

import java.util.List;
import java.util.stream.Collectors;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class QuizResults extends Fragment {
    private TextView congratsText, messageText, yourMistakesText;
    private Button exitButton;
    private ListView mistakesListView;
    private LinearLayout linearLayout;
    private DataCommunicator dataCommunicator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dataCommunicator = (DataCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DataCommunicator");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_results, container, false);

        int score = -1;
        String gifUrl = "";
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            score = bundle.getInt("score", -1);
            gifUrl = bundle.getString("gifUrl", "");
        }

        congratsText = view.findViewById(R.id.congratsText);
        messageText = view.findViewById(R.id.messageText);
        yourMistakesText = view.findViewById(R.id.yourMistakesText);
        exitButton = view.findViewById(R.id.exitButton);
        mistakesListView = view.findViewById(R.id.mistakesList);
        linearLayout = view.findViewById(R.id.linearLayout);

        if(score != 10) {
            final List<QuizGenerator.QuestionWord> mistakesList = dataCommunicator.obtainMistakesList();
            ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_list_item_1,
                    mistakesList.stream().map(word -> word.getLiteralQuestion() + " = " + word.getLiteralAnswer()).collect(Collectors.toList()));
            mistakesListView.setAdapter(arrayAdapter);

            Drawable background = mistakesListView.getBackground();
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable)background).getPaint().setColor(ContextCompat.getColor(getContext(), R.color.results_list_background_color));
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable)background).setColor(ContextCompat.getColor(getContext(), R.color.results_list_background_color));
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable)background).setColor(ContextCompat.getColor(getContext(), R.color.results_list_background_color));
            }
        }else{
            mistakesListView.setVisibility(View.GONE);
            yourMistakesText.setVisibility(View.GONE);
        }

        if(score >= 9) {
            congratsText.setText("Congratulations");
            ImageView imageView = new ImageView(getContext());
            int widthHeight = dpToPx(250);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(widthHeight, widthHeight);
            layoutParams.setMargins(0, dpToPx(50), 0, 0);
            layoutParams.gravity = Gravity.CENTER;
            linearLayout.addView(imageView, layoutParams);

            if(gifUrl.isEmpty()){
                imageView.setBackgroundResource(R.drawable.well_done_backup);
            }else {
                Glide.with(getActivity())
                        .load(gifUrl)
                        .into(imageView);
            }
        }else if(score >= 7){
            congratsText.setText("Good job");
        }else if(score >= 5){
            congratsText.setText("Well enough");
        }else{
            congratsText.setText("You should try harder next time");
        }

        messageText.setText("Your score: " + score + " / 10");

        exitButton.setOnClickListener(view1 -> getActivity().finish());
        return view;
    }

    private int dpToPx(int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getContext().getResources().getDisplayMetrics());
    }

    interface DataCommunicator {
        List<QuizGenerator.QuestionWord> obtainMistakesList();
    }
}