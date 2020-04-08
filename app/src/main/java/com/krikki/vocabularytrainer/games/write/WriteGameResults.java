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
import android.widget.Toast;

import com.krikki.vocabularytrainer.DataStorageManager;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.games.CommonGameGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static com.krikki.vocabularytrainer.util.StringManipulator.isStringSimplifiedFrom;
import static com.krikki.vocabularytrainer.util.StringManipulator.isStringSimplifiedFromWithSingleMistake;

/**
 * This class receives list of questions and their answers. It then verifies whether they are correct
 * and displays the results. If word written as answer is incorrect, but it exists in dictionary (under some other word),
 * this will be displayed in the list with an * and on click it will open info dialog explaining what
 * that word really means. Info dialog works for every answer, but in other cases it just displays
 * one word. This class also adds scores to words and saves them.
 */
public class WriteGameResults extends Fragment {
    private TextView congratsText, messageText, yourMistakesText;
    private Button exitButton;
    private ListView mistakesListView;
    private LinearLayout linearLayout;
    private List<Word> words;
    private List<WriteGame.QuestionAnswerObject> questionAnswerObjects;
    private DataCommunicator dataCommunicator;

    private CommonGameGenerator.GameType questionType;
    private CommonGameGenerator.GameType answerType;

    private List<Boolean> singleMistakeFound = new ArrayList<Boolean>();

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
        questionType = CommonGameGenerator.GameType.valueOf(intent.getStringExtra("gameQuestionType"));
        answerType = CommonGameGenerator.GameType.valueOf(intent.getStringExtra("gameAnswerType"));

        questionAnswerObjects = dataCommunicator.obtainQuestionsAnswersList();
        words = dataCommunicator.obtainWords();

        List<WriteGame.QuestionAnswerObject> mistakesList = findMistakes(questionAnswerObjects);
        writeWordsToStorage(); // to save updated scores

        // in some cases player might have mistaken one word for another
        // this array points to word for which given answer would be correct
        Word[] mistakenWords = new Word[mistakesList.size()];
        for (int i = 0; i < mistakesList.size(); i++) {
            final String answer = mistakesList.get(i).getAnswer();
            if(answer.contains(",")) {
                continue;
            }
            mistakenWords[i] = words.stream().filter(word ->
                answerType.existsInWord.test(word) && Arrays.stream(answerType.get.apply(word)).anyMatch(str -> isStringSimplifiedFrom(str, answer))
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
                    // TODO
                    if(singleMistakeFound.get(i)) {
                        textToDisplay += " <DEBUG-single_mistake>";
                    }
                }
                mistakesToDisplay.add(textToDisplay);
            }

            ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_list_item_1,
                    mistakesToDisplay);
            mistakesListView.setAdapter(arrayAdapter);
            mistakesListView.setOnItemClickListener((adapterView, view12, position, l) ->
                    new WordInfoDialog(getContext(), mistakesList.get(position).getWord(), mistakenWords[position]).show());

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

    private void writeWordsToStorage() {
        DataStorageManager storageManager = new DataStorageManager(getContext());
        try {
            storageManager.writeWordsToStorage(words);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Exception thrown when writing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Finds mistakes in given list of questions and their answers. Every answer can contain multiple words
     * (to show off pretty much), so each word is compared to all correct answers. Word is allowed
     * to have at most 2 mistakes, where 1 mistake is either swapped letters, missing or extra letter.
     * If multiple words are in an answer, only can have a mistake.
     * This method also updates scores in {@link Word}.
     * @param questionAnswerObjects list of questions and their answers
     * @return filtered list of questions and their answers that contains only the ones with mistakes
     */
    private List<WriteGame.QuestionAnswerObject> findMistakes(List<WriteGame.QuestionAnswerObject> questionAnswerObjects) {
        List<WriteGame.QuestionAnswerObject> mistakesList = new LinkedList<>();
        final int tooManyMistakes = 2;

        for(WriteGame.QuestionAnswerObject questionAnswerObject : questionAnswerObjects){
            int mistakes = -1; // mistakes counts differences between strings (0 is identical, string can still be simplified)

            for(String answer : questionAnswerObject.getAnswer().split(",")) {
                int minMistakes = Integer.MAX_VALUE;
                for(String correctAnswer : answerType.get.apply(questionAnswerObject.getWord())){
                    minMistakes = Math.min(minMistakes, isStringSimplifiedFromWithSingleMistake(correctAnswer, answer));
                }
                if(minMistakes < tooManyMistakes && mistakes <= 0){
                    mistakes = minMistakes;
                }else{
                    mistakes = tooManyMistakes;
                    break;
                }
            }

            if(mistakes == 0){
                questionAnswerObject.getWord().addNewScore(10);
            }else{
                mistakesList.add(questionAnswerObject);
                if(mistakes == 1) {
                    singleMistakeFound.add(true);
                    questionAnswerObject.getWord().addNewScore(7);
                }else{
                    singleMistakeFound.add(false);
                    questionAnswerObject.getWord().addNewScore(0);
                }
            }
        }
        return mistakesList;
    }

    private int dpToPx(int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getContext().getResources().getDisplayMetrics());
    }


    interface DataCommunicator {
        List<WriteGame.QuestionAnswerObject> obtainQuestionsAnswersList();
        List<Word> obtainWords();
    }
}