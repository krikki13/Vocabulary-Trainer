package com.krikki.vocabularytrainer.games.quiz;

import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.games.CommonGameGenerator;
import com.krikki.vocabularytrainer.games.GameGeneratorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * QuizGame generator selects words to be used as question and answers. For picking questions it uses
 * an algorithm that prioritizes words with lower scores (and even more undefined scores).
 * When picking false answers to appear in a question, it picks words that are similar to the
 * correct one.
 */
public class QuizGenerator extends CommonGameGenerator {
    private final static int NUMBER_OF_QUESTIONS = 10;

    // List<Word> words is in parent class (from it only the ones with nulls at required places are removed)
    private List<QuestionWord> questions; // list of 10 questions
    private List<List<AnswerWord>> falseAnswers; // list of 10x3 incorrect answers (correct answers are contained in questions)

    @Getter
    private GameType questionType;
    @Getter
    private GameType answerType;

    private int questionNumber = 0;
    private int correctAnswerIndex = -1;

    /**
     * Initiates quiz generator. To get data for first question, use instance methods. To continue to
     * next question call next().
     * @param words list of all words
     * @param questionType type of word that appears in question
     * @param answerType type of word that appears in answer
     * @throws QuizGenerationException if questionType matches answerType or there are insufficient words (less than 20)
     */
    public QuizGenerator(ArrayList<Word> words, GameType questionType, GameType answerType) throws QuizGenerationException {
        super(words);

        if (questionType == answerType) {
            throw new QuizGenerationException("Question type must not match answer type");
        } else if (words == null || words.size() < 2*NUMBER_OF_QUESTIONS) {
            throw new QuizGenerationException("There are too few words to generate a quiz. Have at least " + (2*NUMBER_OF_QUESTIONS) + " words");
        }
        this.questionType = questionType;
        this.answerType = answerType;

        // remove words from words list if words needed fo questions and answers do not exist
        removeWordsThatDoNotContainField(questionType);
        removeWordsThatDoNotContainField(answerType);

        questions = new ArrayList<>(NUMBER_OF_QUESTIONS);
        falseAnswers = new ArrayList<>(NUMBER_OF_QUESTIONS);

        try {
            questions = pickQuestions(NUMBER_OF_QUESTIONS).stream().map(QuestionWord::new).collect(Collectors.toList());
        } catch (GameGeneratorException e) {
            // though this should never happen because QuizGenerator already checks for invalid states
            throw new QuizGenerationException(e.getMessage());
        }
        questions.forEach(question -> {
            question.setLiteralQuestion(oneOf(questionType.get.apply(question.getWord())));
            question.setLiteralAnswer(oneOf(answerType.get.apply(question.getWord())));
        });
        pickAnswers();
    }

    /**
     * Get question string. If question contains multiple words, only one will be returned.
     */
    public String getLiteralQuestion(){
        return questions.get(questionNumber).getLiteralQuestion();
    }

    /**
     * Get list of all answers. The correct one is placed randomly in the array. Its index can be
     * obtained using {@link #getCorrectAnswerIndex()}.
     * @return list of all answers
     */
    public List<String> getAllAnswers(){
        List<String> answers = new ArrayList<>(4);
        falseAnswers.get(questionNumber).forEach(falseAnswer -> answers.add(falseAnswer.literalAnswer));
        if(correctAnswerIndex == -1)
            correctAnswerIndex = (int) (Math.random() * 4);
        answers.add(correctAnswerIndex, questions.get(questionNumber).getLiteralAnswer());
        return answers;
    }

    /**
     * Get list of answers translated. These are the correct matches for each answer obtained by
     * {@link #getAllAnswers()}.
     * @return list of answers translated
     */
    public List<String> getAnswersTranslated(){
        List<String> answers = new ArrayList<>(4);
        falseAnswers.get(questionNumber).forEach(falseAnswer -> answers.add(arrayToPrettyString(questionType.get.apply(falseAnswer.word))));
        if(correctAnswerIndex == -1)
            correctAnswerIndex = (int) (Math.random() * 4);
        answers.add(correctAnswerIndex, arrayToPrettyString(questionType.get.apply(questions.get(questionNumber).getWord())));
        return answers;
    }

    /**
     * Returns {@link QuestionWord} object that is currently a question.
     */
    public QuestionWord getQuestionWord(){
        return questions.get(questionNumber);
    }

    /**
     * Get number of current question.
     */
    public int getQuestionNumber(){
        return questionNumber;
    }

    /**
     * Index of correct answer in the array given by {@link #getAllAnswers()}.
     * This number is set to -1 before calling {@link #getAllAnswers()}.
     */
    public int getCorrectAnswerIndex(){
        return correctAnswerIndex;
    }

    /**
     * Go to next question. To check if there are any more use {@link #hasNext()}.
     * @throws NoSuchElementException if there are no more questions
     */
    public void next() {
        if(questionNumber == NUMBER_OF_QUESTIONS){
            throw new NoSuchElementException("No more questions available");
        }else{
            questionNumber++;
            correctAnswerIndex = -1;
        }
    }

    /**
     * Returns true if there are any questions left.
     * @return
     */
    public boolean hasNext() {
        return questionNumber + 1 < NUMBER_OF_QUESTIONS;
    }


    private void pickAnswers() {
        List<AnswerWord> answerList = words.stream()
                .map(AnswerWord::new).collect(Collectors.toList());
        for (int i = 0; i < questions.size(); i++) {
            final Word.WordType answerWordType = questions.get(i).getWord().getWordType();
            final String correctAnswer = questions.get(i).getLiteralAnswer();

            // this function compares words in various categories to decide which one looks more
            // like the question
            ToIntBiFunction<String, String> wordComparator = (a, b) -> {
                // compare by first letter
                if(a.charAt(0) == correctAnswer.charAt(0) && b.charAt(0) != correctAnswer.charAt(0)){
                    return 1;
                }else if(a.charAt(0) != correctAnswer.charAt(0) && b.charAt(0) == correctAnswer.charAt(0)){
                    return -1;
                }
                // compare by length
                if(correctAnswer.length() >= 8){
                    // long words
                    if(a.length() >= 6 && b.length() < 6){
                        return 1;
                    }else if(a.length() < 6 && b.length() >= 6){
                        return -1;
                    }
                }else{
                    // short or middle words
                    int allowedDifference;
                    if(correctAnswer.length() <= 5){
                        allowedDifference = 2;
                    }else{
                        allowedDifference = 3;
                    }
                    if(Math.abs(a.length()-correctAnswer.length()) <= allowedDifference && Math.abs(b.length()-correctAnswer.length()) > allowedDifference){
                        return 1;
                    }else if(Math.abs(a.length()-correctAnswer.length()) > allowedDifference && Math.abs(b.length()-correctAnswer.length()) <= allowedDifference){
                        return -1;
                    }
                }
                int counterA = 0;
                int counterB = 0;
                int counterAback = 0;
                int counterBback = 0;
                for (int j = 1; j < 4 && j < a.length() && j < b.length() && j < correctAnswer.length(); j++) {
                    if(a.charAt(j) == correctAnswer.charAt(j)) counterA++;
                    if(b.charAt(j) == correctAnswer.charAt(j)) counterB++;
                    if(a.charAt(a.length()-j) == correctAnswer.charAt(correctAnswer.length()-j)) counterAback++;
                    if(b.charAt(b.length()-j) == correctAnswer.charAt(correctAnswer.length()-j)) counterBback++;
                }
                if(counterA >= 2 && counterB < 2){
                    return 1;
                }else if(counterA < 2 && counterB >= 2){
                    return -1;
                }
                if(counterAback >= 2 && counterBback < 2){
                    return 1;
                }else if(counterAback < 2 && counterBback >= 2){
                    return -1;
                }
                return a.compareTo(b);
            };

            answerList.forEach(answerWord ->
                answerWord.setLiteralAnswer(Arrays.stream(answerType.get.apply(answerWord.word)).min(wordComparator::applyAsInt).get()
            ));

            answerList.sort((a, b) -> {
                // first category: word type
                if(answerWordType != null) {
                    Word.WordType wordTypeA = a.word.getWordType();
                    Word.WordType wordTypeB = b.word.getWordType();
                    if (answerWordType.equals(wordTypeA) && !answerWordType.equals(wordTypeB)) {
                        return 1;
                    } else if (!answerWordType.equals(wordTypeA) && answerWordType.equals(wordTypeB)) {
                        return -1;
                    }
                }
                return wordComparator.applyAsInt(a.getLiteralAnswer(), b.getLiteralAnswer());
            });

            int answersPicked = 0;
            List<AnswerWord> finalFalseAnswers = new ArrayList<>(3);
            while(answersPicked < 3){
                int limit = Math.max(answerList.size(), Math.min(Math.max(5, answerList.size() / 3), 45));
                int randomIndex = (int) (Math.random() * limit);
                AnswerWord answerWord = answerList.get(randomIndex);
                if(!finalFalseAnswers.contains(answerWord) &&
                        !answerWord.getWord().getId().equals(questions.get(i).getWord().getId()) &&
                        !answerWord.getLiteralAnswer().equals(correctAnswer)){
                    finalFalseAnswers.add(answerWord);
                    answersPicked++;
                }
            }
            falseAnswers.add(finalFalseAnswers);
        }
    }

    /**
     * This class wraps word and adds String that is exact String that appeared in question.
     * This is needed because Word object can have multiple words and each time only is picked.
     */
    class AnswerWord {
        private Word word;
        @Getter @Setter
        private String literalAnswer;

        private AnswerWord(Word word){
            this.word = word;
        }
        public Word getWord(){
            return word;
        }
    }

    /**
     * Similar as its parent AnswerWord, except that it also adds field literal question.
     */
    class QuestionWord extends AnswerWord{
        @Getter @Setter
        private String literalQuestion;

        private QuestionWord(Word word){
            super(word);
        }
    }
}
