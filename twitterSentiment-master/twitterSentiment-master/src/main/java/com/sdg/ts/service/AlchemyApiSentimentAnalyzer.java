package com.sdg.ts.service;


import com.sdg.ts.model.Mood;
import com.sdg.ts.model.Sentiment;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


public class AlchemyApiSentimentAnalyzer implements SentimentAnalyzer {

    @Value("${alchemy.api.key}")
    private String apiKey;

    private static final Logger log = LoggerFactory.getLogger(AlchemyApiSentimentAnalyzer.class);

    @Override
    public Sentiment analyze(String text) {

        try {

            URI uri = buildUri(text);
            log.debug("Sending request : " + uri.toString());

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpClient httpclient = new DefaultHttpClient();
            String responseBody = httpclient.execute(httpPost, responseHandler);

            log.debug("Response body : " + responseBody);

            Sentiment sentiment = parseSentiment(responseBody);
            if (sentiment == null) {
                return null;
            }

            log.info("result: " + sentiment);

            return sentiment;

        } catch (URISyntaxException e) {
            log.error("Exception building uri", e);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return null;
    }

    private Sentiment parseSentiment(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseJson = mapper.readValue(responseBody, Map.class);

        String status = (String) responseJson.get("status");

        if (!"ok".equalsIgnoreCase(status)) {
            String error = (String) responseJson.get("statusInfo");
            log.error("Error using api: {}", error);
            return null;
        }

        Map<String, String> docSentiment = (Map<String, String>) responseJson.get("docSentiment");

        String moodString = docSentiment.get("type");
        Mood mood = Mood.valueOf(moodString.toString().toUpperCase());
        String scoreString = docSentiment.get("score");
        float confidence = StringUtils.isEmpty(scoreString) ? 0 : Float.valueOf(scoreString);
        Sentiment result = new Sentiment(mood, confidence);
        return result;
    }


    private URI buildUri(String text) throws URISyntaxException {

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost("access.alchemyapi.com")
                .setPath("/calls/text/TextGetTextSentiment")
                .setParameter("apikey", apiKey)
                .setParameter("text", text)
                .setParameter("outputMode", "json");

        return builder.build();
    }


    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
