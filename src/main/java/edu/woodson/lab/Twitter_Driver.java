package edu.woodson.lab;// Name:
// Date:

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.ApacheHttpClient;
import edu.woodson.util.trello.TrelloForTwitter;
import edu.woodson.util.trello.util.MovedCard;
import edu.woodson.util.trello.util.TrelloList;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Twitter_Driver {
    public static void main(String[] args) {
        try {
            PrintStream consolePrint = System.out;

            // PART III - Connect
            // set classpath, edit properties file
            @SuppressWarnings("SpellCheckingInspection")
            Configuration builder = new ConfigurationBuilder()
                    .setDebugEnabled(true)
                    .setOAuthConsumerKey("egk7giRtajHZYXBbKAjzSVhWb")
                    .setOAuthConsumerSecret("bLiAVPF6q14TIztSlsatAKRGUenRKQC5MKtWEfAB8xTfqweeHr")
                    .setOAuthAccessToken("1059514626868740096-T5G4cTrK6NExdx1j33ss69X9ODnMq4")
                    .setOAuthAccessTokenSecret("pHoPJfLiWHtNbEUmx0vOnFdMnn6O4Ajvpvs3IcJkwDeAY")
                    .build();

            Twitter twitter = new TwitterFactory(builder).getInstance();
            TJTwitter bigBird = new TJTwitter(twitter);

            // Part III - Tweet
            // Create and set a String called message below
            // Uncomment this line to test, but then re-comment so that the same
            // tweet does not get sent out over and over.

//      String message="Go Colonials!";
//      twitter.updateStatus(message);

      /*
      String message="Go Colonials!";
      bigBird.tweetOut(message);
      */

            // PART III - Test
            // Choose a public Twitter user's handle

            Scanner scan = new Scanner(System.in);
            consolePrint.print("Please enter a Twitter handle, do not include the @symbol --> ");
            String twitter_handle = scan.next();

            // Find and print the most popular word they tweet
            while (!twitter_handle.equals("done")) {
                bigBird.queryHandle(new Paging(1, 200), twitter_handle);
                consolePrint.println("The most common word from @" + twitter_handle + " is: " + bigBird.getMostPopularWord() + ".");
                consolePrint.println("The word appears " + bigBird.getMaxFrequency() + " times.");
                consolePrint.println();
                consolePrint.print("Please enter a Twitter handle, do not include the @ symbol --> ");
                twitter_handle = scan.next();
            }

            // PART IV
            //bigBird.investigate();
            Timer timer = new Timer();

            final String API_KEY = "6a888dffccb4c413711d7d617057fa07";
            final String TOKEN = "186d9044cd5dfdb60c3b5ab3befb2aaeb2daddccdd05244ce152743701fec680";

            System.out.println("Enter the Trello board id to listen to: ");
            String boardId = scan.next();

            Trello trello = new TrelloImpl(API_KEY, TOKEN, new ApacheHttpClient());


            final Board[] board = new Board[1];
            final TrelloList[] oldTrelloList = new TrelloList[1];

            final int[] time = {0};
            final boolean[] initial = {true};
            TimerTask listenForMovedCards = new TimerTask() {
                @Override
                public void run() {
                    if (initial[0]) {
                        board[0] = trello.getBoard(boardId);
                        List<TList> oldLists = board[0].fetchLists();
                        oldTrelloList[0] = TrelloForTwitter.getTrelloList(trello, oldLists);
                        initial[0] = false;
                    }


                    if (time[0] == 3) {
                        initial[0] = true;

                        Board newBoard = trello.getBoard(boardId);
                        List<TList> newLists = newBoard.fetchLists();
                        TrelloList newTrelloList = TrelloForTwitter.getTrelloList(trello, newLists);

                        MovedCard moved = TrelloForTwitter.findMovedCard(board[0], oldTrelloList[0], newTrelloList);

                        if (moved != null) {
                            try {
                                bigBird.tweetOut("You moved " + moved.getName() + " from " + moved.getFrom().getName() + " to " + moved.getTo().getName());
                            } catch (TwitterException e) {
                                e.printStackTrace();
                            }
                        }

                        time[0] = 0;
                    }
                    time[0]++;
                }
            };

            timer.scheduleAtFixedRate(listenForMovedCards, 0, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

