package com.sdg.ts.sandbox;

import twitter4j.*;

import java.io.IOException;

public class StreamTest {

    public static void main(String[] args) throws TwitterException, IOException {
        StatusListener listener = new StatusListener(){
            public void onStatus(Status status) {
                System.out.println(status.getUser().getName() + " : " + status.getText());
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
            }

            @Override
            public void onStallWarning(StallWarning warning) {
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);

        FilterQuery fq = new FilterQuery();
        fq.track(new String[] {"Iron Man"});

        twitterStream.filter(fq);

        // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
       // twitterStream.sample();
    }

}
