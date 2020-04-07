package com.krikki.vocabularytrainer.games.write;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.games.CommonGameGenerator;
import com.krikki.vocabularytrainer.games.CommonGameGenerator.GameType;
import com.krikki.vocabularytrainer.games.GameGeneratorException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.fragment.app.Fragment;
import lombok.Getter;
import lombok.Setter;

import static com.krikki.vocabularytrainer.games.CommonGameGenerator.oneOf;

public class WriteGame extends Fragment {
    private static final int NUMBER_OF_QUESTIONS = 10;
    private GameControlActivity gameControlActivity;
    private LinearLayout mainLayout;
    private Button doneButton;
    private List<TextView> questionsTextViewList = new ArrayList<>(NUMBER_OF_QUESTIONS);
    private List<EditText> answersEditTextList = new ArrayList<>(NUMBER_OF_QUESTIONS);

    private List<Word> words = new ArrayList<>();
    List<QuestionAnswerObject> questionsAndAnswers;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            gameControlActivity = (GameControlActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement GameControlActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_write_game, container, false);

        mainLayout = view.findViewById(R.id.mainLayout);
        doneButton = view.findViewById(R.id.doneButton);

        Intent intent = getActivity().getIntent();
        GameType questionType = GameType.valueOf(intent.getStringExtra("gameQuestionType"));
        GameType answerType = GameType.valueOf(intent.getStringExtra("gameAnswerType"));

        // Generate TextViews and EditTexts
        for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
            // get styles
            ContextThemeWrapper textViewThemeWrapper = new ContextThemeWrapper(getContext(), R.style.WriteGameQuestionText);
            ContextThemeWrapper editTextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.WriteGameAnswerEditText);
            // extract linear params related styles from ContextThemeWrapper
            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(textViewThemeWrapper, null);
            LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(editTextThemeWrapper, null);

            TextView textView = new TextView(textViewThemeWrapper, null, 0);
            EditText editText = new EditText(editTextThemeWrapper, null, 0);

            mainLayout.addView(textView, 2*i, textViewParams);
            mainLayout.addView(editText, 2*i+1, editTextParams);
            questionsTextViewList.add(textView);
            answersEditTextList.add(editText);
        }

        words = gameControlActivity.getWordList();
        CommonGameGenerator gameGenerator = new CommonGameGenerator(words);
        gameGenerator.removeWordsThatDoNotContainField(questionType);
        gameGenerator.removeWordsThatDoNotContainField(answerType);
        try {
            questionsAndAnswers = gameGenerator.pickQuestions(NUMBER_OF_QUESTIONS).stream().map(QuestionAnswerObject::new).collect(Collectors.toList());
        } catch (GameGeneratorException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
            Word word = questionsAndAnswers.get(i).getWord();
            String textToDisplay = oneOf(questionType.get.apply(word));
            String note = questionType.getNote.apply(word);
            if(!note.isEmpty()){
                textToDisplay += " (" + note + ")";
            }
            questionsAndAnswers.get(i).setLiteralQuestion(textToDisplay);
            questionsTextViewList.get(i).setText(textToDisplay);
        }

        doneButton.setOnClickListener(view1 -> {
            for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
                String text = answersEditTextList.get(i).getText().toString();
                questionsAndAnswers.get(i).setAnswer(text);
            }
            gameControlActivity.onGameFinished(questionsAndAnswers);
        });
        return view;
    }

    interface GameControlActivity {
        List<Word> getWordList();
        void onGameFinished(List<QuestionAnswerObject> questionsAndAnswers);
    }

    class QuestionAnswerObject {
        @Getter
        private Word word;
        @Getter @Setter
        private String literalQuestion;
        @Getter @Setter
        private String answer;

        public QuestionAnswerObject(Word word) {
            this.word = word;
        }
    }
}