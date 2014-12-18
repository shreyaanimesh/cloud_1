package com.sdg.ts.service;


import com.google.common.base.CharMatcher;
import com.sdg.ts.model.Mood;
import com.sdg.ts.model.Sentiment;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


public class ViralHeatSentimentAnalyzer implements SentimentAnalyzer {

    @Value("${viralheat.api.key}")
    private String apiKey;

    private static final Logger log = LoggerFactory.getLogger(ViralHeatSentimentAnalyzer.class);

    @Override
    public Sentiment analyze(String text) {

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost("app.viralheat.com")
                .setPath("/social/api/sentiment")
                .setParameter("api_key", apiKey)
                .setParameter("text", text);

        try {

            URI uri = builder.build();
            log.debug("Sending request : " + uri.toString());

            HttpGet httpget = new HttpGet(uri);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpClient httpclient = new DefaultHttpClient();
            String responseBody = httpclient.execute(httpget, responseHandler);

            log.debug("Response body : " + responseBody);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseJson = mapper.readValue(responseBody, Map.class);


            int status = (Integer) responseJson.get("status");

            if (status != 200) {
                String error = (String) responseJson.get("error");
                log.error("Error using api: {}", error);
                return null;
            }

            String moodString = (String) responseJson.get("mood");
            moodString = CharMatcher.is('\'').removeFrom(moodString);

            Mood mood = Mood.valueOf(moodString.toString().toUpperCase());
            float confidence = Float.valueOf(responseJson.get("prob").toString());
            Sentiment result = new Sentiment(mood, confidence);

            return result;



        } catch (URISyntaxException e) {
            log.error("Exception building uri", e);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return null;

    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


}
