package com.krikki.vocabularytrainer.games;

import com.krikki.vocabularytrainer.Word;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

import lombok.Getter;

/**
 * Provides method @{link #pickQuestions} for picking words for question out of List of words. This
 * method prioritizes words with lower scores.
 *
 * It also provides enum {@link GameType} that specifies which field of word is being used for current game.
 */
public class CommonGameGenerator {
    @Getter
    protected List<Word> words;

    /**
     * Specifies which data of word will be used as a question or answer. It also provides
     * methods for obtaining data from currently required fields in word. For example if
     * gameType is PRIMARY_LANG, calling gameType.get.apply(someWord) will return english words for someWord.
     */
    public enum GameType {
        PRIMARY_LANG(word -> true, Word::getWords, Word::getNote, Word::getDemand, Word::getSynonyms),
        SECONDARY_LANG(Word::hasTranslatedWords, Word::getTranslatedWords, Word::getTranslatedNote, Word::getTranslatedDemand, Word::getTranslatedSynonyms),
        DESCRIPTION(Word::hasDescription, word -> new String[]{word.getDescription()}, word -> "", word -> "", word -> null);

        /**
         * Predicate that returns true when word contains needed data for given question or answer type.
         * So predicate for SECONDARY_LANG will check if translated word exists in word.
         * For PRIMARY_LANG it always returns true.
         */
        public Predicate<Word> existsInWord;
        /**
         * Function that gets one word from primary or translated word or the description
         */
        public Function<Word, String[]> get;
        /**
         * Function that gets note for given game type. It returns empty String if it does not exist
         * or game type is DESCRIPTION.
         */
        public Function<Word, String> getNote;
        /**
         * Function that gets demand for given game type. It returns empty String if it does not exist
         * or game type is DESCRIPTION.
         */
        public Function<Word, String> getDemand;
        /**
         * Function that gets synonyms for given game type. It returns null if it does not exist
         * or game type is DESCRIPTION.
         */
        public Function<Word, String[]> getSynonyms;

        GameType(Predicate<Word> existsInWord, Function<Word, String[]> get, Function<Word, String> getNote,
                 Function<Word, String> getDemand, Function<Word, String[]> getSynonyms) {
            this.existsInWord = existsInWord;
            this.get = get;
            this.getNote = getNote;
            this.getDemand = getDemand;
            this.getSynonyms = getSynonyms;
        }
    }

    public CommonGameGenerator(List<Word> words) {
        this.words = new ArrayList<>(words);
    }

    /**
     * Removes words from List this.words that are missing field described in parameter.
     * For example if gameType is DESCRIPTION, all words that are missing description will be removed.
     * @param gameType field that is required in words
     */
    public void removeWordsThatDoNotContainField(GameType gameType){
        if (gameType != GameType.PRIMARY_LANG) {
            this.words.removeIf(gameType.existsInWord.negate());
        }
    }

    /**
     * Returns n picked words from list. Probability to pick a word grows, when its score decreases.
     * Word score must be an int with values 0-100 or -1 which indicates undefined score (it is even more likely to be picked).
     * Note that this method sorts the list by word score.
     *
     * Words are picked from sorted list. Traversing list is weighted according to words score.
     * When scores are low, weights are also low and it is easy to traverse. Weights are obtained by using mapping function
     * on word scores. To pick n random words from list, 10 numbers are picked between 0 and total sum of weights.
     * Whenever accumulated sum of weights (when traversing) passes randomly picked number, current word is picked.
     * Using this algorithm it may happen that 2 numbers point to last word in the list. In that case remaining words
     * are the ones with lowest scores in the list (that have not been picked yet).
     * @throws GameGeneratorException if there are fewer words in the list than should be for given parameter
     */
    public List<Word> pickQuestions(int numberOfWords) throws GameGeneratorException {
        if(numberOfWords > words.size()){
            throw new GameGeneratorException("There are fewer words in the list than should be for given parameter");
        }
        this.words.sort(Word.comparatorByScore());
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
        ArrayList<Word> pickedWords = new ArrayList<>(numberOfWords);
        TreeSet<Integer> weightSelectors = new TreeSet<>();
        for (int i = 0; i < numberOfWords; i++) {
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

        return pickedWords;
    }

    public static String oneOf(String... array) {
        return array[(int) (Math.random() * array.length)];
    }

    public static String arrayToPrettyString(String[] array){
        return String.join(", ", array);
    }


}


