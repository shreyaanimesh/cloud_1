package com.sdg.ts.service;

import com.sdg.ts.model.Tweet;

public interface TweetSink {

    public void accept(Tweet tweet);
    public void done();
}
