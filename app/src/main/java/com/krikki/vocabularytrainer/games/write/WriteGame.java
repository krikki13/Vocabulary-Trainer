package com.krikki.vocabularytrainer.games.write;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.krikki.vocabularytrainer.R;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

public class WriteGame extends Fragment {
    private static final int NUMBER_OF_QUESTIONS = 10;
    private LinearLayout mainLayout;
    private Button doneButton;
    private List<TextView> questionsTextViewList = new ArrayList<>(NUMBER_OF_QUESTIONS);
    private List<EditText> answersEditTextList = new ArrayList<>(NUMBER_OF_QUESTIONS);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_write_game, container, false);

        mainLayout = view.findViewById(R.id.mainLayout);
        doneButton = view.findViewById(R.id.doneButton);

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

            textView.setText("TextView "+i);
            editText.setText("EditText "+i);
            mainLayout.addView(textView, 2*i, textViewParams);
            mainLayout.addView(editText, 2*i+1, editTextParams);
            questionsTextViewList.add(textView);
            answersEditTextList.add(editText);
        }


        return view;
    }
}