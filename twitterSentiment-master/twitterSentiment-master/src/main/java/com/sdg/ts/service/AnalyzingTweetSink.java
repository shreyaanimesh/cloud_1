package com.sdg.ts.service;


import com.sdg.ts.model.Sentiment;
import com.sdg.ts.model.Tweet;
import com.sdg.ts.repos.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyzingTweetSink implements TweetSink {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzingTweetSink.class);

    @Autowired
    private TweetRepository tweetRepository;

    private SentimentAnalyzer analyzer;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SentimentStats stats = new SentimentStats();

    @Override
    public void accept(Tweet tweet) {
        logger.info("{} : {}", tweet.getUsername(), tweet.getText());
        executor.execute(new AnalyzeWorker(tweet));
    }

    public void done() {
       logger.info("Analysis Complete");
       logger.info(stats.toString());
    }

    public SentimentAnalyzer getAnalyzer() {
        return analyzer;
    }

    @Required
    public void setAnalyzer(SentimentAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    class AnalyzeWorker implements Runnable {

        private final Tweet tweet;

        AnalyzeWorker(Tweet tweet) {
            this.tweet = tweet;
        }

        @Override
        public void run() {

            Tweet existing = tweetRepository.findByStatusId(tweet.getStatusId());
            if (existing != null) {
                logger.warn("Already seen this tweet, skipping");
                return;
            }

            Sentiment sentiment = analyzer.analyze(tweet.getText());
            logger.info("{} Sentiment: {}", tweet.getText(), sentiment);

            if (sentiment != null) {
                stats.add(sentiment);
                tweet.setSentiment(sentiment);
                tweetRepository.save(tweet);
            }
        }
    }

}

