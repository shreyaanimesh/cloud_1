package com.sdg.ts.service;

import com.sdg.ts.model.Tweet;
import com.sdg.ts.repos.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PersistingTweetSink implements TweetSink {

    private static final Logger logger = LoggerFactory.getLogger(PersistingTweetSink.class);

    @Autowired
    private TweetRepository tweetRepository;

    @Override
    public void accept(Tweet tweet) {
        logger.info("{} : {}", tweet.getUsername(), tweet.getText());
        tweetRepository.save(tweet);
    }

    @Override
    public void done() {
    }
}
