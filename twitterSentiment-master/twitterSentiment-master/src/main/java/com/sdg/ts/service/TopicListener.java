package com.sdg.ts.service;

import com.sdg.ts.model.Topic;
import com.sdg.ts.model.Tweet;
import com.sdg.ts.repos.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import twitter4j.*;

import javax.annotation.PostConstruct;
import java.util.Date;

public class TopicListener {

    private static final Logger log = LoggerFactory.getLogger(TopicListener.class);

    private Topic topic;

    @Autowired
    private TweetSink tweetSink;

    @Autowired
    private TweetRepository tweetRepository;

    public Topic getTopic() {
        return topic;
    }

    @Required
    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @PostConstruct
    public void post() {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(new SimpleStatusListener(twitterStream));

        FilterQuery fq = new FilterQuery();
        fq.track(new String[]{topic.getName()});
        twitterStream.filter(fq);
    }

    class SimpleStatusListener implements StatusListener {

        private final TwitterStream stream;
        int count = 0;

        private final static int MAX_COUNT  = 20;

        public SimpleStatusListener(TwitterStream stream) {
            this.stream = stream;
        }

        public void onStatus(Status status) {
            if (count++ > MAX_COUNT) {
                tweetSink.done();
                this.stream.shutdown();
            }

            Tweet tweet = new Tweet();
            tweet.setUsername(status.getUser().getScreenName());
            tweet.setDate(new Date());
            tweet.setText(status.getText());
            tweet.setStatusId(status.getId());
            tweetSink.accept(tweet);

        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            long statusId = statusDeletionNotice.getStatusId();
            tweetRepository.deleteByStatusId(statusId);
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            log.warn("Track Limitation Notice :  {}", numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            //not tracked, can be ignored
        }

        @Override
        public void onStallWarning(StallWarning warning) {
            log.warn("StallWarning: {}", warning.getMessage());
        }

        @Override
        public void onException(Exception ex) {
            log.error("Exception reading stream", ex);
        }


    }

}
