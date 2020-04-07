package com.krikki.vocabularytrainer.games.write;

import android.content.Context;
import android.content.Intent;
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

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.games.CommonGameGenerator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class WriteGameResults extends Fragment {
    private TextView congratsText, messageText, yourMistakesText;
    private Button exitButton;
    private ListView mistakesListView;
    private LinearLayout linearLayout;
    private List<Word> words;
    private List<WriteGame.QuestionAnswerObject> questionAnswerObjects;
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

        congratsText = view.findViewById(R.id.congratsText);
        messageText = view.findViewById(R.id.messageText);
        yourMistakesText = view.findViewById(R.id.yourMistakesText);
        exitButton = view.findViewById(R.id.exitButton);
        mistakesListView = view.findViewById(R.id.mistakesList);
        linearLayout = view.findViewById(R.id.linearLayout);

        Intent intent = getActivity().getIntent();
        CommonGameGenerator.GameType questionType = CommonGameGenerator.GameType.valueOf(intent.getStringExtra("gameQuestionType"));
        CommonGameGenerator.GameType answerType = CommonGameGenerator.GameType.valueOf(intent.getStringExtra("gameAnswerType"));

        questionAnswerObjects = dataCommunicator.obtainQuestionsAnswersList();
        words = dataCommunicator.obtainWords();

        List<WriteGame.QuestionAnswerObject> mistakesList = questionAnswerObjects.stream().filter(qa ->
            Arrays.stream(answerType.get.apply(qa.getWord())).noneMatch(str -> Word.isStringSimplifiedFrom(str, qa.getAnswer()))
        ).collect(Collectors.toList());

        // in some cases player might have mistaken one word for another
        // this array points to word for which given answer would be correct
        Word[] mistakenWords = new Word[mistakesList.size()];
        for (int i = 0; i < mistakesList.size(); i++) {
            final String answer = mistakesList.get(i).getAnswer();
            if(answer.contains(",")) {
                continue;
            }
            mistakenWords[i] = words.stream().filter(word ->
                answerType.existsInWord.test(word) && Arrays.stream(answerType.get.apply(word)).anyMatch(str -> Word.isStringSimplifiedFrom(str, answer))
            ).findAny().orElse(null);
        }


        int score = questionAnswerObjects.size() - mistakesList.size();

        if(score != 10) {
            List<String> mistakesToDisplay = new LinkedList<>();
            for (int i = 0; i < mistakesList.size(); i++) {
                WriteGame.QuestionAnswerObject qa = mistakesList.get(i);
                String textToDisplay = qa.getLiteralQuestion() + " = " + String.join(", ", answerType.get.apply(qa.getWord()));
                if(!qa.getAnswer().isEmpty()) {
                    if(mistakenWords[i] == null) {
                        textToDisplay += " (not " + qa.getAnswer() + ")";
                    }else{
                        textToDisplay += " (not " + qa.getAnswer() + ")*";
                    }
                }
                mistakesToDisplay.add(textToDisplay);
            }

            ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_list_item_1,
                    mistakesToDisplay);
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

            imageView.setBackgroundResource(R.drawable.well_done_backup);
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
        List<WriteGame.QuestionAnswerObject> obtainQuestionsAnswersList();
        List<Word> obtainWords();
    }
}