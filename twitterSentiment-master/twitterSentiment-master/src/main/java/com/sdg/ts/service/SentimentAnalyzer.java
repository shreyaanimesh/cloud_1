package com.sdg.ts.service;

import com.sdg.ts.model.Sentiment;


public interface SentimentAnalyzer {

    public Sentiment analyze(String text);

}
