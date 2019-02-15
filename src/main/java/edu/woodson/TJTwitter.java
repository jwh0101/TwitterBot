package edu.woodson;

import twitter4j.*;
import twitter4j.Status;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class TJTwitter {
    private final Twitter twitter;
    private final List<Status> statuses;
    private List<String> words;
    private int numberOfTweets;
    private String popularWord;
    private int frequencyMax;

    public TJTwitter(PrintStream console) {
        // Makes an instance of Twitter - this is re-usable and thread safe.
        // Connects to Twitter and performs authorizations.
        twitter = TwitterFactory.getSingleton();
        statuses = new ArrayList<>();
        words = new ArrayList<>();
    }

    public List<String> getWords() {
        return words;
    }

    public int getNumberOfTweets() {
        return numberOfTweets;
    }

    public String getMostPopularWord() {
        return popularWord;
    }

    public int getFrequencyMax() {
        return frequencyMax;
    }

    /*****************  Part III - Tweet *******************/
    /**
     * This method tweets a given message.
     *
     * @param message a message you wish to Tweet out
     */
    public twitter4j.Status tweetOut(String message) throws TwitterException {
        return twitter.updateStatus(message);
    }


    /******************  Part III - Test *******************/
    /**
     * This method queries the tweets of a particular user's handle.
     *
     * @param handle the Twitter handle (username) without the @sign
     */
    @SuppressWarnings("unchecked")
    public void queryHandle(String handle) throws TwitterException, IOException {
        statuses.clear();
        words.clear();
        fetchTweets(handle);
        words = splitIntoWords();
        removeCommonEnglishWords();
        sortAndRemoveEmpties();
        mostPopularWord();
    }

    /**
     * This method fetches the most recent 2,000 tweets of a particular user's handle and
     * stores them in an arrayList of Status objects.  Populates statuses.
     *
     * @param handle the Twitter handle (username) without the @sign
     */
    public void fetchTweets(String handle) throws TwitterException, IOException {
        // Creates file for debugging purposes
        PrintStream out = new PrintStream(new FileOutputStream("tweets.txt"));
        Paging page = new Paging(1, 200);
        int p = 1;
        while (p <= 10) {
            page.setPage(p);
            statuses.addAll(twitter.getUserTimeline(handle, page));
            p++;
        }
        numberOfTweets = statuses.size();
        out.println("Number of tweets = " + numberOfTweets);
    }

    /**
     * This method takes each status and splits them into individual words.
     * Store the word in words.
     */
    public List<String> splitIntoWords() {
        return statuses.stream()
                .map(Status::getText)
                .map(StringTokenizer::new)
                .map(Enumeration::asIterator)
                .map(objectIterator -> (Iterable<Object>) () -> objectIterator)
                .map(Iterable::spliterator)
                .flatMap(spliterator -> StreamSupport.stream(spliterator, false))
                .map(Object::toString)
                .collect(Collectors.toList());

    }

    /**
     * This method removes common English words from the list of words.
     * Remove all words found in commonWords.txt  from the argument list.
     * The count will not be given in commonWords.txt. You must count the number of words in this method.
     * This method should NOT throw an exception.  Use try/catch.
     */
    @SuppressWarnings("unchecked")
    public void removeCommonEnglishWords() throws IOException {

        //your code goes here
        URL url = getClass().getResource("commonWords.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        Set<String> commonWords = reader.lines().collect(Collectors.toSet());
        reader.close();

        words.removeAll(commonWords);
    }

    /**
     * This method sorts the words in words in alphabetically (and lexicographic) order.
     * You should use your sorting code you wrote earlier this year.
     * Remove all empty strings while you are at it.
     */
    @SuppressWarnings("unchecked")
    public List<String> sortAndRemoveEmpties() {
        words.sort(String::compareTo);
        words.removeIf(s -> s.trim().isEmpty());
        return words;

    }

    /**
     * This method calculates the word that appears the most times
     * Consider case - should it be case sensitive?  The choice is yours.
     *
     * @post will populate the frequencyMax variable with the frequency of the most common word
     */
    @SuppressWarnings("unchecked")
    public Optional<Integer> mostPopularWord() {
        Map<String, Integer> wordMap = new HashMap<>();
        words.stream()
                .map(String::toLowerCase)
                .forEach(word -> {
                    int value = wordMap.containsKey(word)
                            ? wordMap.get(word) + 1
                            : 0;
                    wordMap.put(word, value);
                });

        return wordMap.values().stream().max(Integer::compareTo);

    }

    /**
     * This method removes common punctuation from each individual word.
     * This method changes everything to lower case.
     * Consider reusing code you wrote for a previous lab.
     * Consider if you want to remove the # or @ from your words. Could be interesting to keep (or remove).
     *
     * @ param String  the word you wish to remove punctuation from
     * @ return String the word without any punctuation, all lower case
     */
    public String removePunctuation(String s) {
        return s.replaceAll("[.!?\\-]", "");
    }

    /******************  Part IV *******************/
    public void investigate() {
        //Enter your code here
        //TODO: complete
    }

    /**
     * This method determines how many people in Arlington, VA
     * tweet about the Miami Dolphins.  Hint:  not many. :(
     */
    public void sampleInvestigate() {
        Query query = new Query("Miami Dolphins");
        query.setCount(100);
        query.setGeoCode(new GeoLocation(38.8372839, -77.1082443), 5, Query.MILES);
        query.setSince("2015-12-1");
        try {
            QueryResult result = twitter.search(query);
            System.out.println("Count : " + result.getTweets().size());
            for (Status tweet : result.getTweets()) {
                System.out.println("@" + tweet.getUser().getName() + ": " + tweet.getText());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        System.out.println();
    }
}
