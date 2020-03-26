package com.krikki.vocabularytrainer.games.quiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static com.krikki.vocabularytrainer.games.quiz.QuizGenerator.QuizType;

/**
 * Controls quiz game.
 */
public class QuizGame extends Fragment {
    private QuizEventListener quizEventListener;
    private TextView question;
    private List<Button> buttonAnswers;
    private Button buttonNext;
    private ArrayList<Word> words;
    private QuizGenerator quizGenerator;
    private int score = 0;
    private boolean buttonsDisabled = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            quizEventListener = (QuizEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_quiz_game, container, false);

        question = view.findViewById(R.id.tvQuestion);
        buttonAnswers = new ArrayList<>();
        buttonAnswers.add(view.findViewById(R.id.bAnswer1));
        buttonAnswers.add(view.findViewById(R.id.bAnswer2));
        buttonAnswers.add(view.findViewById(R.id.bAnswer3));
        buttonAnswers.add(view.findViewById(R.id.bAnswer4));
        buttonNext = view.findViewById(R.id.buttonNext);

        buttonAnswers.get(0).setOnClickListener(view1 -> onAnswerClick(0));
        buttonAnswers.get(1).setOnClickListener(view1 -> onAnswerClick(1));
        buttonAnswers.get(2).setOnClickListener(view1 -> onAnswerClick(2));
        buttonAnswers.get(3).setOnClickListener(view1 -> onAnswerClick(3));
        buttonNext.setVisibility(View.GONE);
        buttonNext.setOnClickListener(view1 -> {
            if(quizGenerator.hasNext()) {
                buttonsDisabled = false;
                buttonAnswers.forEach(button -> setButtonBackgroundColor(button, R.color.defaultButtonBackgroundColor));
                buttonNext.setVisibility(View.GONE);
                quizGenerator.next();
                showQuestion();

                if(!quizGenerator.hasNext()) {
                    quizEventListener.quizNearlyFinished(score);
                    buttonNext.setText("Done");
                }
            }else{
                writeWordsToStorage();
                quizEventListener.quizFinished(score);
            }
        });

        readListOfWordsFromStorage();
        try {
            Intent intent = getActivity().getIntent();
            QuizType questionType = QuizType.valueOf(intent.getStringExtra("quizQuestionType"));
            QuizType answerType = QuizType.valueOf(intent.getStringExtra("quizAnswerType"));
            quizGenerator = new QuizGenerator(words, questionType, answerType);
            showQuestion();
        } catch (QuizGenerationException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        return view;
    }

    private void showQuestion(){
        question.setText(quizGenerator.getLiteralQuestion());
        final List<String> answers = quizGenerator.getAllAnswers();
        for (int i = 0; i < 4; i++) {
            buttonAnswers.get(i).setText(answers.get(i));
        }
    }

    private void setButtonBackgroundColor(Button button, int color){
        Drawable background = button.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable)background).getPaint().setColor(ContextCompat.getColor(getActivity(), color));
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable)background).setColor(ContextCompat.getColor(getActivity(), color));
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable)background).setColor(ContextCompat.getColor(getActivity(), color));
        }
    }

    private void onAnswerClick(int buttonIndex){
        if(!buttonsDisabled){
            if (buttonIndex == quizGenerator.getCorrectAnswerIndex()) {
                setButtonBackgroundColor(buttonAnswers.get(buttonIndex), R.color.correctBackgroundColor);
                score++;
                quizGenerator.getQuestionWord().addNewScore(Word.MAX_INDIVIDUAL_SCORE);
            } else {
                setButtonBackgroundColor(buttonAnswers.get(buttonIndex), R.color.incorrectBackgroundColor);
                setButtonBackgroundColor(buttonAnswers.get(quizGenerator.getCorrectAnswerIndex()), R.color.correctBackgroundColor);
                quizGenerator.getQuestionWord().addNewScore(Word.MIN_INDIVIDUAL_SCORE);
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
        DataStorageManager storageManager = new DataStorageManager(getActivity());
        try {
            String wordsText = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            words = storageManager.convertToListOfWords(wordsText);
        } catch (IOException | JSONException e) {
            Toast.makeText(getActivity(), "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
            getActivity().finish();
        } catch (Word.DuplicatedIdException | Word.UnsuccessfulWordCreationException e1){
            Toast.makeText(getActivity(), "Data file is incorrectly formatted. Please go to dictionary for more options", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    private void writeWordsToStorage(){
        try {
            DataStorageManager storageManager = new DataStorageManager(getActivity());
            storageManager.writeToStorage(DataStorageManager.WORDS_FILE, storageManager.convertToJson(words));
        } catch (IOException | JSONException | Word.UnsuccessfulWordCreationException e) {
            e.printStackTrace();
        }
    }

    interface QuizEventListener {
        void quizNearlyFinished(int intermediateScore);
        void quizFinished(int totalScore);
    }
}
