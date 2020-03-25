package com.krikki.vocabularytrainer.games.quiz;

import com.krikki.vocabularytrainer.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public class QuizGenerator {
    /**
     * Specifies which data of word will be used as a question or answer.
     */
    public enum WordType {
        PRIMARY_LANG(word -> true, word -> oneOf(word.getWords())),
        SECONDARY_LANG(Word::hasTranslatedWords, word -> oneOf(word.getTranslatedWords())),
        DESCRIPTION(Word::hasDescription, Word::getDescription);

        /**
         * Predicate that returns true when word contains needed data for given question or answer type.
         * So predicate for SECONDARY_LANG will check if translated word exists in word.
         * For PRIMARY_LANG it always returns true.
         */
        private Predicate<Word> existsInWord;
        /**
         * Function that gets one word from primary or translated word or the description
         */
        private Function<Word, String> getOne;

        WordType(Predicate<Word> existsInWord, Function<Word, String> getOne) {
            this.existsInWord = existsInWord;
            this.getOne = getOne;
        }
    }

    private ArrayList<Word> words; // list of all words from which only the ones with nulls at required places are removed
    private ArrayList<Word> questions; // list of 10 questions
    private ArrayList<ArrayList<Word>> falseAnswers; // list of 10x3 incorrect answers (correct answers are contained in questions)

    private ArrayList<String> literalQuestions; // actual text that is displayed TODO or is it?

    private WordType questionType;
    private WordType answerType;

    public QuizGenerator(ArrayList<Word> words, WordType questionType, WordType answerType) throws QuizGenerationException {
        if (questionType == answerType) {
            throw new QuizGenerationException("Question type must not match answer type");
        } else if (words.size() < 30) {
            throw new QuizGenerationException("There are too few words to generate a quiz. Have at least 30 words");
        }
        this.questionType = questionType;
        this.answerType = answerType;

        this.words = new ArrayList<>(words);
        if (questionType != WordType.PRIMARY_LANG) {
            this.words.removeIf(questionType.existsInWord.negate());
        }
        if (answerType != WordType.PRIMARY_LANG) {
            this.words.removeIf(answerType.existsInWord.negate());
        }
        this.words.sort(Word.comparatorByScore());
    }

    private void pickQuestions() {
        // find distribution of scores in words (result is array of avg values for each tenth of the word score list)
        // length 10
        final int[] distribution = new int[10];
        int i = 0;
        int dec = 1;
        final int size = words.size();
        int sum = 0;
        int count = 0;
        for (Word word : words) {
            if (i == dec * size / 10) {
                distribution[dec - 1] = sum / count;
                dec++;
                sum = 0;
                count = 0;
            }
            count++;
            sum += word.getScore();
            i++;
        }
        distribution[9] = sum / count;

        // generate probability arraylist where values represent tenths of word array from which question will be picked
        // length >10, values: 0-9
        final ArrayList<Integer> probabilities = new ArrayList<>();
        final int divideBy = Word.MAX_TOTAL_SCORE / 10;
        for (int j = 0; j < distribution.length; j++) {
            int numOfCopies = (Word.MAX_TOTAL_SCORE - distribution[j]) / divideBy;
            numOfCopies = Math.max(numOfCopies, 1);
            probabilities.addAll(Collections.nCopies(numOfCopies, j));
        }

        final Random random = new Random();
        final TreeSet<Integer> wordNumbers = new TreeSet<>();
        final int tenthLength = words.size() / 10;
        for (int k = 0; k < 10; k++) {
            int tenth = probabilities.get(random.nextInt(probabilities.size()));
            int wordInTenth = random.nextInt(tenthLength);
            if (!wordNumbers.add(tenth * tenthLength + wordInTenth)) {
                k--;
            }
        }

        wordNumbers.forEach(wordNumber -> questions.add(words.get(wordNumber)));
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
     * @param words list of words
     */
    private void pickQuestions(List<Word> words) {
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

        questions.addAll(pickedWords);
    }

    private static String oneOf(String... array) {
        return array[(int) (Math.random() * array.length)];
    }

    private void pickAnswers() {
        throw new UnsupportedOperationException();
        // TODO
    }

}
