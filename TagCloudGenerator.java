import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;
import components.utilities.Reporter;

/**
 * Program to take a text file and output an HTML file
 *
 * @author Antonio Ortiz
 */
public final class TagCloudGenerator {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
        // no code needed here
    }

    private static final String SEPARATORS = "\t\n\r,-.!?[];:/() '";

    private static int Min_Value = 0;
    private static int Max_Value = 0;

    /**
     * A method designed to prompt the user for a desired string
     *
     * @param out
     * @return returnString - name of the txt file given
     */
    private static String getInFileName(SimpleWriter out) {
        String returnString;
        SimpleReader in = new SimpleReader1L();
        out.println("Please enter the desired in-file name(path included):");

        returnString = in.nextLine();

        return returnString;
    }

    /**
     * A method designed to prompt the user for a desired string
     *
     * @param out
     * @return returnString - name of the html file to output
     */
    private static String getOutFileName(SimpleWriter out) {

        String returnString;
        SimpleReader in = new SimpleReader1L();
        out.println("Please enter the desired out-file name(path included):");

        returnString = in.nextLine();

        return returnString;
    }

    /**
     * Reports the first "new word".
     *
     * @param text
     *            the string to be tested
     * @param position
     *            position to begin looping through text
     * @return substring of text
     * @updates text
     * @requires |text| >= 1
     * @ensures substring of text is the next word
     *
     */
    private static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        // TODO - fill in body
        int endIndex = text.length();
        boolean isSep = SEPARATORS.indexOf(text.charAt(position)) == -1;
        int i = position + 1;
        while (i < endIndex
                && (SEPARATORS.indexOf(text.charAt(i)) == -1) == isSep) {

            i++;
        }

        return text.substring(position, i);

    }

    /**
     * Gets all of the words from the file
     *
     * @param fileName
     * @return Queue<String> of words
     */
    private static Queue<String> getWordsFromFile(String fileName) {
        Queue<String> wordList = new Queue1L<String>();
        SimpleReader inFromFile = new SimpleReader1L(fileName);
        while (!inFromFile.atEOS()) {
            String currentLine = inFromFile.nextLine();
            currentLine = currentLine.toLowerCase(); //ensures they are all same case
            int pos = 0;
            while (pos < currentLine.length()) {
                String tempString = nextWordOrSeparator(currentLine, pos);
                pos += tempString.length();
                if (SEPARATORS.indexOf(tempString.charAt(0)) == -1) {
                    wordList.enqueue(tempString);
                }
            }
            pos = 0;

        }
        return wordList;
    }

    /**
     * Counts how many times each word occurs.
     *
     * @param wordList
     *            the list of words to check
     * @return map of words and occurrences
     * @restores wordList
     * @requires |wordList| >= 1
     * @ensures howMany contains all words and their occurrences
     *
     */
    private static Map<String, Integer> getMapWithCount(
            Queue<String> wordList) {
        Map<String, Integer> mapWithCount = new Map1L<>();
        String word;
        int queueLength = wordList.length();

        int value = 1;
        while (queueLength > 0) {
            word = wordList.dequeue();
            if (mapWithCount.hasKey(word)) {
                int key = mapWithCount.value(word);
                int newKey = key + 1; // if word is already in map, increase occurrence by 1
                mapWithCount.replaceValue(word, newKey);
            } else {
                mapWithCount.add(word, value); // adds words and count of 1
            }
            wordList.enqueue(word); // restores contents of queue
            queueLength--;
        }

        return mapWithCount;
    }

    /**
     * Counts how many times each word occurs.
     *
     * @param wordList
     *            the list of words to check
     * @return map of words and occurrences
     * @restores wordList
     * @requires |wordList| >= 1
     * @ensures howMany contains all words and their occurrences
     *
     */
    private static Queue<String> getListNoDuplicates(
            Map<String, Integer> wordList) {
        Queue<String> listNoDuplicates = new Queue1L<String>();
        Map<String, Integer> tempMap = new Map1L<String, Integer>();

        String word;
        int counter = 0;
        while (wordList.size() > 0) {
            Map.Pair<String, Integer> tempPair = wordList.removeAny();
            listNoDuplicates.enqueue(tempPair.key());
            tempMap.add(tempPair.key(), tempPair.value());
        }
        wordList.transferFrom(tempMap);
        return listNoDuplicates;
    }

    /**
     * Alphabetizes words in queue.
     *
     * @param wordList
     *            list of words from text
     * @return sorted list of words
     * @updates wordList
     * @requires |wordList| >= 1
     * @ensures words in queue are alphabetized ignoring case
     *
     */
    private static class alphaSort // alphabetize for our sorting machine
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> one,
                Map.Pair<String, Integer> two) {
            return one.key().compareToIgnoreCase(two.key());

        }
    }

    private static class numberSort //number for our sorting machine
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> one,
                Map.Pair<String, Integer> two) {
            return two.value().compareTo(one.value());

        }
    }

    /**
     *
     * @param mapWithAllWords
     *            map with count of occurrences
     * @param numberSort
     *            comparator for the num sort
     * @param numOfTerms
     *            The number of terms the user wanted
     * @return queueOfWordByOccurrences
     */
    private static Queue<String> orderMapByOccurrences(
            Map<String, Integer> mapWithAllWords, Comparator numberSort,
            int numOfWordsWanted) {

        Queue<String> queueOfWordByOccurrences = new Queue1L<String>();
        Map<String, Integer> tempMap = new Map1L<String, Integer>();
        Reporter.assertElseFatalError(
                numOfWordsWanted <= mapWithAllWords.size(),
                "Error: Tag cloud size exceeds number of words");
        SortingMachine<Map.Pair<String, Integer>> sort = new SortingMachine1L<>(
                numberSort);

        while (mapWithAllWords.size() > 0) {
            sort.add(mapWithAllWords.removeAny()); // adds words from map into sorting machine
        }

        sort.changeToExtractionMode();

        Map.Pair<String, Integer> currentWordPairWithCount = sort.removeFirst();
        Max_Value = currentWordPairWithCount.value();

        tempMap.add(currentWordPairWithCount.key(),
                currentWordPairWithCount.value()); //adding it to tempMap
        queueOfWordByOccurrences.enqueue(currentWordPairWithCount.key());

        for (int currentWord = 0; currentWord < numOfWordsWanted; currentWord++) {
            currentWordPairWithCount = sort.removeFirst();
            queueOfWordByOccurrences.enqueue(currentWordPairWithCount.key());
            Min_Value = currentWordPairWithCount.value();

            tempMap.add(currentWordPairWithCount.key(),
                    currentWordPairWithCount.value()); //addingToTempMap
            System.out.println(currentWord);
        }
        mapWithAllWords.transferFrom(tempMap);
        return queueOfWordByOccurrences;
    }

    /**
     *
     * @param mapWithCount
     *            map with count of occurrences
     * @param alphaSort
     *            the queue sorted alphabetically
     * @param numOfTerms
     *            The number of terms the user wanted
     * @return return the alphabetized queue
     */
    private static Queue<String> getQueueAlpha(
            Map<String, Integer> mapWithCount, Comparator alphaSort,
            int numOfTerms) {
        Map<String, Integer> tempMap = new Map1L<String, Integer>();
        Queue<String> alphaQueue = new Queue1L<String>();
        SortingMachine<Map.Pair<String, Integer>> sort = new SortingMachine1L<>(
                alphaSort);

        int minVal = 0;
        while (mapWithCount.size() > 0) {
            sort.add(mapWithCount.removeAny()); // adds words from map into sorting machine
        }
        sort.changeToExtractionMode();
        Map.Pair<String, Integer> tempPair = sort.removeFirst();
        alphaQueue.enqueue(tempPair.key());
        tempMap.add(tempPair.key(), tempPair.value());
        for (int wordCount = 0; wordCount < numOfTerms - 1; wordCount++) {
            tempPair = sort.removeFirst();
            alphaQueue.enqueue(tempPair.key());

            tempMap.add(tempPair.key(), tempPair.value());
        }

        mapWithCount.transferFrom(tempMap);
        return alphaQueue;
    }

    /**
     *
     * @param mapWithCount
     *            map with count of occurrences
     * @param alphaSort
     *            the queue sorted alphabetically
     * @param numOfTerms
     *            The number of terms the user wanted
     *
     * @param outputFileName
     *            name of the output file
     * @param numWords
     *            numebr of words the user wants
     * @param inName
     *            input file name
     */
    private static void createHTML(Queue<String> alphaQueue,
            Map<String, Integer> mapWithWords, String outputFileName,
            int numWords, String inName) {
        SimpleWriter out = new SimpleWriter1L(outputFileName);

        out.println("<html>");
        out.println("<head>");
        out.println("<title> Top " + numWords + " words in  " + inName
                + "</title>");
        out.println(
                "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Top " + numWords + " words in " + inName + "</h2>");
        out.println("<hr>");
        out.println("<div class =\"cdiv\">");
        out.println("<p class=\"cbox\">");

        while (alphaQueue.length() > 0) {

            String word = alphaQueue.dequeue();
            font(word, mapWithWords.value(word), inName, out);
        }

        out.println("</p>");
        out.println("</div>");
        out.println("<body>");
        out.println("</html>");
    }

    /**
     *
     * @param word
     *            The current word you want to print
     * @param times
     *            the number of occurrences of that word
     * @param name
     *            The name of the of the file
     *
     */
    private static void font(String word, int times, String name,
            SimpleWriter out) {
        // out.println("min = " + min + " max is: " + max);

        int fontSize = (48 - 11) * (times - Min_Value); //formula given
        fontSize /= (Max_Value - Min_Value);
        fontSize += 11;
        out.println("<span style=\"cursor:default\" class=\"f" + fontSize
                + "\" title=\"count: " + times + "\">" + word + "</span>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        SimpleReader in = new SimpleReader1L();
        //creating the two necessary scanners

        String inputFileName = getInFileName(out);
        String outputFileName = getOutFileName(out);
        out.println("How many words would you like to see in your Tag Cloud?");
        //THESE LINES ARE FOR TESTING ONLY

        String tempIn = in.nextLine();
        int numWords = Integer.parseInt(tempIn);

        // this is all just initial setup - getting files
        // names and the number of words

        Queue<String> listOfAllWordsWithDuplicates = getWordsFromFile(
                inputFileName);
        // this is a list with every word and all of the duplicates as well

        Map<String, Integer> mapWithCount = getMapWithCount(
                listOfAllWordsWithDuplicates);

        Queue<String> listOfAllWordsNoDuplicates = getListNoDuplicates(
                mapWithCount);

        Comparator<Map.Pair<String, Integer>> numberSort = new numberSort();
        Comparator<Map.Pair<String, Integer>> alphaSort = new alphaSort();

        Queue<String> wordsInOrderByNum = orderMapByOccurrences(mapWithCount,
                numberSort, numWords);

        Queue<String> alphaQueue = getQueueAlpha(mapWithCount, alphaSort,
                numWords);
        createHTML(alphaQueue, mapWithCount, outputFileName, numWords,
                inputFileName);
    }

}
