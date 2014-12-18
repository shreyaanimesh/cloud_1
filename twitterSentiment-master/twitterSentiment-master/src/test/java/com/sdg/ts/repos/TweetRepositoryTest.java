package com.sdg.ts.repos;

import com.sdg.ts.model.Sentiment;
import com.sdg.ts.model.Tweet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/context-services.xml")
@Transactional
public class TweetRepositoryTest {

    @Qualifier("tweetRepository")
    @Autowired
    private TweetRepository tweetRepository;

    @Test
    public void testFindByStatusId() throws Exception {

        Tweet tweet = new Tweet();
        tweet.setText("Test");
        tweet.setStatusId(Long.valueOf(42));
        tweet.setSentiment(Sentiment.neutral());
        tweetRepository.save(tweet);

        Tweet found = tweetRepository.findByStatusId(tweet.getStatusId());
        assertNotNull(found);

    }

    @Test
    public void testDeleteByStatusIdAnd() throws Exception {

        Tweet tweet = new Tweet();
        tweet.setText("Test");
        tweet.setStatusId(Long.valueOf(42));
        tweet.setSentiment(Sentiment.neutral());

        tweetRepository.save(tweet);
        assertEquals(1, tweetRepository.count());

        tweetRepository.deleteByStatusId(tweet.getStatusId());
        assertEquals(0, tweetRepository.count());


    }
}
