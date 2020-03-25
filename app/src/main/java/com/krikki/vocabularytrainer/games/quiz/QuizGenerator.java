package com.krikki.vocabularytrainer.games.quiz;

import com.krikki.vocabularytrainer.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Function;
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

    /**
     * Selects 10 words from word list as questions and adds them to questions field. Words are picked
     * according to their score. List is divided to 10 sections which are valued by average score of
     * contained words. This represents the probability for words to be selected from that section.
     * At least 30 words are required, otherwise activity is finished.
     */
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

    private static String oneOf(String... array) {
        return array[(int) (Math.random() * array.length)];
    }

    private void pickAnswers() {
        throw new UnsupportedOperationException();
        // TODO
    }

}
