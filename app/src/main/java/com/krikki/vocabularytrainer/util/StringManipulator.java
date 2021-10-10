package com.krikki.vocabularytrainer.util;

import static com.krikki.vocabularytrainer.Word.letterSimplified;

/**
 * This class provides various utility methods that work on and with Strings.
 */
public final class StringManipulator {
    /**
     * Returns true if simplifiedString word is a simplified version of baseString or its leading substring (starting from 0).
     * String is simplified when localized letters (like in baseString) are replaced with the most similar
     * letters from english alphabet. Therefore this method returns true if cevapi is simplified from čevapi,
     * but not the other way around. Note that simplifiedString can still contain localized letters.
     * For mapping localized letters to their closest english letter, {@link com.krikki.vocabularytrainer.Word#letterSimplified} is used.
     *
     * @param baseString       base string which needs to be evaluated (must not be shorter than simplifiedString)
     * @param simplifiedString string which is used for evaluation
     * @return true if simplifiedString is simplified leading substring of baseString
     * @throws StringIndexOutOfBoundsException if baseString is shorter than simplifiedString
     */
    public static boolean isSubstringSimplifiedFrom(String baseString, String simplifiedString) {
        for (int i = 0; i < simplifiedString.length(); i++) {
            if (!isCharSimplifiedFrom(baseString.charAt(i),simplifiedString.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if simplifiedString word is a simplified version of baseString.
     * String is simplified when localized letters (like in baseString) are replaced with the most similar
     * letters from english alphabet. Therefore this method returns true if cevapi is simplified from čevapi,
     * but not the other way around. Note that simplifiedString can still contain localized letters.
     * For mapping localized letters to their closest english letter,
     * {@link com.krikki.vocabularytrainer.Word#letterSimplified} is used.
     *
     * If you need to compare substring to a string, use {@link #isSubstringSimplifiedFrom(String, String)}
     * instead.
     *
     * @param baseString       base string which needs to be evaluated
     * @param simplifiedString string which is used for evaluation
     * @return true if simplifiedString is simplified string of baseString
     */
    public static boolean isStringSimplifiedFrom(String baseString, String simplifiedString) {
        if(baseString.length() != simplifiedString.length()) {
            return false;
        }
        return isSubstringSimplifiedFrom(baseString, simplifiedString);
    }

    /**
     * Verifies if simplifiedString word is a simplified version of baseString, where it allows a single mistake.
     * A mistake is a swapped letter, a missing one or an extra one. All of these count as one mistake.
     * This method returns 0 if strings are identical (or simplified) or 1 if a mistake was found.
     * Everything above that is 2.
     *
     * String is simplified when localized letters (like in baseString) are replaced with the most similar
     * letters from english alphabet. Therefore this method returns true if cevapi is simplified from čevapi,
     * but not the other way around. Note that simplifiedString can still contain localized letters.
     * For mapping localized letters to their closest english letter,
     * {@link com.krikki.vocabularytrainer.Word#letterSimplified} is used.
     *
     * @param baseString       base string which needs to be evaluated
     * @param simplifiedString string which is used for evaluation
     * @return true if simplifiedString is simplified string of baseString
     */
    public static int isStringSimplifiedFromWithSingleMistake(String baseString, String simplifiedString) {
        if(Math.abs(baseString.length() - simplifiedString.length()) > 1) {
            return 2;
        }
        int length = Math.max(baseString.length(), simplifiedString.length());

        // Append character to avoid StringIndexOutOfBoundsException a little more easily
        baseString += "**";
        simplifiedString += "**";
        boolean mistakeWasMade = false;

        int b = 0;
        int s = 0;
        while(b < length && s < length){
            if(!isCharSimplifiedFrom(baseString.charAt(b), simplifiedString.charAt(s))){
                if(mistakeWasMade){
                    return 2;
                }

                mistakeWasMade = true;
                boolean both = false;
                if(isCharSimplifiedFrom(baseString.charAt(b), simplifiedString.charAt(s+1))){
                    s++;
                    both = true;
                }
                if(isCharSimplifiedFrom(baseString.charAt(b+1), simplifiedString.charAt(s))){
                    b++;
                    if(both){
                        continue;
                    }
                }
            }
            b++;
            s++;
        }
        return mistakeWasMade ? 1 : 0;
    }

    private static boolean isCharSimplifiedFrom(char baseChar, char simplifiedChar){
        if (baseChar != simplifiedChar) {
            if (letterSimplified.containsKey((int)baseChar)) {
                if (letterSimplified.get((int)baseChar) != simplifiedChar) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
