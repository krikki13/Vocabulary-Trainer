package com.krikki.vocabularytrainer.games.quiz;

import com.krikki.vocabularytrainer.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
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
public class QuizGenerator {
    private final static int NUMBER_OF_QUESTIONS = 10;
    /**
     * Specifies which data of word will be used as a question or answer.
     */
    public enum QuizType {
        PRIMARY_LANG(word -> true, Word::getWords),
        SECONDARY_LANG(Word::hasTranslatedWords, Word::getTranslatedWords),
        DESCRIPTION(Word::hasDescription, word -> new String[]{word.getDescription()});

        /**
         * Predicate that returns true when word contains needed data for given question or answer type.
         * So predicate for SECONDARY_LANG will check if translated word exists in word.
         * For PRIMARY_LANG it always returns true.
         */
        private Predicate<Word> existsInWord;
        /**
         * Function that gets one word from primary or translated word or the description
         */
        private Function<Word, String[]> get;

        QuizType(Predicate<Word> existsInWord, Function<Word, String[]> get) {
            this.existsInWord = existsInWord;
            this.get = get;
        }
    }

    private List<Word> words; // list of all words from which only the ones with nulls at required places are removed
    private List<QuestionWord> questions; // list of 10 questions
    private List<List<AnswerWord>> falseAnswers; // list of 10x3 incorrect answers (correct answers are contained in questions)

    @Getter
    private QuizType questionType;
    @Getter
    private QuizType answerType;

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
    public QuizGenerator(ArrayList<Word> words, QuizType questionType, QuizType answerType) throws QuizGenerationException {
        if (questionType == answerType) {
            throw new QuizGenerationException("Question type must not match answer type");
        } else if (words.size() < 2*NUMBER_OF_QUESTIONS) {
            throw new QuizGenerationException("There are too few words to generate a quiz. Have at least " + (2*NUMBER_OF_QUESTIONS) + " words");
        }
        this.questionType = questionType;
        this.answerType = answerType;

        this.words = new ArrayList<>(words);
        // remove words from words list if words needed fo questions and answers do not exist
        if (questionType != QuizType.PRIMARY_LANG) {
            this.words.removeIf(questionType.existsInWord.negate());
        }
        if (answerType != QuizType.PRIMARY_LANG) {
            this.words.removeIf(answerType.existsInWord.negate());
        }
        this.words.sort(Word.comparatorByScore());

        questions = new ArrayList<>(NUMBER_OF_QUESTIONS);
        falseAnswers = new ArrayList<>(NUMBER_OF_QUESTIONS);

        pickQuestions();
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

    /**
     * Returns n picked words from list. Probability to pick a word grows, when its score decreases.
     * Word score must be an int with values 0-100 or -1 which indicates undefined score (it is even more likely to be picked).
     *
     * Words are picked from sorted list. Traversing list is weighted according to words score.
     * When scores are low, weights are also low and it is easy to traverse. Weights are obtained by using mapping function
     * on word scores. To pick n random words from list, 10 numbers are picked between 0 and total sum of weights.
     * Whenever accumulated sum of weights (when traversing) passes randomly picked number, current word is picked.
     * Using this algorithm it may happen that 2 numbers point to last word in the list. In that case remaining words
     * are the ones with lowest scores in the list (that have not been picked yet).
     */
    private void pickQuestions() {
        final int numberOfQuestions = 10;
        final IntUnaryOperator scoreToWeight = score -> {
            int s = 110 - score;
            if(score == -1)
                s += 15;
            if(score < 25)
                s += 10;
            if(score < 50)
                s += 10;
            if(score > 90)
                s -= 5;
            return s;
        };
        // generate random numbers in range 0-(sumOfAll)
        int sumOfAll = words.stream().mapToInt(word -> scoreToWeight.applyAsInt(word.getScore())).sum();
        ArrayList<Word> pickedWords = new ArrayList<>(numberOfQuestions);
        TreeSet<Integer> weightSelectors = new TreeSet<>();
        for (int i = 0; i < numberOfQuestions; i++) {
            if(!weightSelectors.add((int)(Math.random() * sumOfAll))) {
                i--;
            }
        }

        // traverse the list and pick words from it
        Iterator<Integer> weightSelectorIterator = weightSelectors.iterator();
        ListIterator<Word> wordIterator = words.listIterator();
        int totalAccumulatedWeight = 0;
        while (weightSelectorIterator.hasNext() && wordIterator.hasNext()) {
            final int nextAccumulatedWeight = weightSelectorIterator.next();
            while (true) {
                final Word currentWord = wordIterator.next();
                totalAccumulatedWeight += scoreToWeight.applyAsInt(currentWord.getScore());
                if (totalAccumulatedWeight >= nextAccumulatedWeight) {
                    pickedWords.add(currentWord);
                    break;
                }
            }
        }
        // if two or more random numbers point to last word, wordIterator will be done, but randomNumberIterator wont
        // in that case just pick first n words from words list (which were not yet picked)
        wordIterator = words.listIterator();
        while (weightSelectorIterator.hasNext()) {
            weightSelectorIterator.next();
            while (wordIterator.hasPrevious()) {
                final Word currentWord = wordIterator.previous();
                if (!pickedWords.contains(currentWord)) {
                    pickedWords.add(currentWord);
                    break;
                }
            }
        }

        questions = pickedWords.stream().map(QuestionWord::new).collect(Collectors.toList());
        questions.forEach(question -> {
            question.setLiteralQuestion(oneOf(questionType.get.apply(question.getWord())));
            question.setLiteralAnswer(oneOf(answerType.get.apply(question.getWord())));
        });
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
                for (int j = 1; j < 4 && j < a.length() && j < b.length(); j++) {
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

    private static String oneOf(String... array) {
        return array[(int) (Math.random() * array.length)];
    }

    private static String arrayToPrettyString(String[] array){
        return String.join(", ", array);
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
