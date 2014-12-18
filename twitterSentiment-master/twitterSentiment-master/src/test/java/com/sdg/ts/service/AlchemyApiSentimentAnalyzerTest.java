package com.sdg.ts.service;

import com.sdg.ts.model.Mood;
import com.sdg.ts.model.Sentiment;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class AlchemyApiSentimentAnalyzerTest {

    @Configuration
    static class ContextConfiguration {

        @Bean
        public AlchemyApiSentimentAnalyzer sentimentAnalyzer() {
            return new AlchemyApiSentimentAnalyzer();
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigurer(Environment env) {
            PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
            bean.setLocation(new ClassPathResource("alchemyapi.properties"));
            return bean;
        }

    }

    @Autowired
    private AlchemyApiSentimentAnalyzer sentimentAnalyzer;

    @Test
    public void sanityCheck() throws Exception {

        Sentiment sentiment = sentimentAnalyzer.analyze("Wow. I don't mean to be mean but I really didn't enjoy Iron Man 3. Super let down.");
        Assert.assertNotNull(sentiment);
        Assert.assertEquals(Mood.POSITIVE, sentiment.getMood());

    }
}
