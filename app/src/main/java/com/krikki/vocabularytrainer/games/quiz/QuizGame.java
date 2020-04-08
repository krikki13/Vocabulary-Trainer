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
import java.util.LinkedList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static com.krikki.vocabularytrainer.games.CommonGameGenerator.GameType;

/**
 * Controls quiz game.
 */
public class QuizGame extends Fragment {
    private final static int NUMBER_OF_POINTS_FOR_CORRECT = 7;

    private QuizEventListener quizEventListener;
    private TextView question;
    private List<Button> buttonAnswers;
    private Button buttonNext;
    private ArrayList<Word> words;
    private QuizGenerator quizGenerator;
    private List<QuizGenerator.QuestionWord> mistakesList = new LinkedList<>();
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
                quizGenerator.next();
                showQuestion();

                if(!quizGenerator.hasNext()) {
                    quizEventListener.quizNearlyFinished(score);
                    buttonNext.setText("Done");
                }
            }else{
                writeWordsToStorage();
                quizEventListener.quizFinished(score, mistakesList);
            }
        });

        readListOfWordsFromStorage();
        try {
            Intent intent = getActivity().getIntent();
            GameType questionType = GameType.valueOf(intent.getStringExtra("gameQuestionType"));
            GameType answerType = GameType.valueOf(intent.getStringExtra("gameAnswerType"));
            quizGenerator = new QuizGenerator(words, questionType, answerType);
            showQuestion();
        } catch (QuizGenerationException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        return view;
    }

    private void showQuestion(){
        buttonsDisabled = false;
        buttonAnswers.forEach(button -> setButtonBackgroundColor(button, R.color.defaultButtonBackgroundColor));
        buttonNext.setVisibility(View.GONE);

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
                quizGenerator.getQuestionWord().getWord().addNewScore(NUMBER_OF_POINTS_FOR_CORRECT);
            } else {
                final QuizGenerator.QuestionWord questionWord = quizGenerator.getQuestionWord();
                setButtonBackgroundColor(buttonAnswers.get(buttonIndex), R.color.incorrectBackgroundColor);
                setButtonBackgroundColor(buttonAnswers.get(quizGenerator.getCorrectAnswerIndex()), R.color.correctBackgroundColor);
                questionWord.getWord().addNewScore(Word.MIN_INDIVIDUAL_SCORE);
                mistakesList.add(questionWord);
            }
            buttonNext.setVisibility(View.VISIBLE);
            buttonsDisabled = true;

            int i = 0;
            String delimiter = " = ";
            if(quizGenerator.getQuestionType() == GameType.DESCRIPTION || quizGenerator.getAnswerType() == GameType.DESCRIPTION){
                delimiter = "\n=\n";
            }
            for (String correctTranslationForAnswer : quizGenerator.getAnswersTranslated()) {
                buttonAnswers.get(i++).append(delimiter + correctTranslationForAnswer);
            }
        }
    }

    public List<QuizGenerator.QuestionWord> getMistakesList(){
        return mistakesList;
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
        /**
         * This is called when last question is being shown. It can be used for preparations for
         * results activity.
         * @param intermediateScore score to this point (1 more can be gained until the end)
         */
        void quizNearlyFinished(int intermediateScore);

        /**
         * This is called when quiz is finished.
         * @param totalScore total score from quiz
         * @param mistakesList list of mistakes (questions and correct answers; not the answers that were clicked)
         */
        void quizFinished(int totalScore, List<QuizGenerator.QuestionWord> mistakesList);
    }
}
