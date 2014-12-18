# Applying Sentiment Analysis to Twitter

Although social media has enabled consumers and businesses to connect like never before, it offers its own set of challenges. The sheer flood of information makes it difficult to perform informed decisions. For instance, how is the brand manager of Acme Corp supposed to handle the launch of the new "WonderWidget" line?   There could be tens of thousands of tweets that need analyzing. Not to mention Facebook comments, YouTube, Pinterest and Amazon reviews. This is where Sentiment Analysis comes in. 

Sentiment Analysis is the process of taking a block of text and determining if the author feels positive, neutral, or negative about a particular topic. It can be an extremely difficult problem to do correctly. For instance, consider the following (naive) approach. This approach takes simply takes a dictionary of words, assigning a positive or negative weight to each. To determine the overall sentiment of a phrase, simply add up the scores of the words found in the dictionary. Here is an example:

| Keyword     		| Sentiment |
|---------------|----------:|
| Excellent	    | 10        | 
| Impressed	    |  6        |
| Great        	|  5        |
| OK            |  1        |
| Meh           |  0        |
| Boring        |  -3       |
| Terrible      |  -5       |
| Sick          | -4        |
| Hitler-esqe   | -9999     |

Now, taking this table, we can assign values to the following sentences:

_The movie was **great**! **Excellent** explosions!_   5 + 10 = 15

_I though the movie was **terrible**. **Boring**!_   -5 + -3  = -8

So far so good, for these completely contrived and unrealistic examples. But notice how quickly we run into trouble. For instance, the word “sick” can mean different things in context:

_I got **sick** after I ate Taco Bell._

_These rhymes are **sick**!_

Our naïve approach would treat both of these as negative sentiments, even though the latter is expressing approval of some “sick rhymes”.
Additionally, the subject of phrase greatly affects how what a positive viewpoint means. For instance, I’d love to read an unpredictable mystery novel, but would stay away from a car with unpredictable brakes.

Clearly the dictionary approach has some significant limitations. In order to do a proper job of sentiment analysis, some natural language processing needs to be done. Natural language processing takes human input, whether textual or spoken, and converts it to a form where a computer can infer some meaning. However, going into the methods and science of natural language processing is beyond the scope of this article, and frankly, well beyond the understanding of this author. The good news is, there are several companies that provide this service. 

For the purposes of this article, we’ll be using [AlchemyAPI](http://www.alchemyapi.com/). ![Alchemy Api Logo](http://www.alchemyapi.com/sites/default/files/Logo60Height.png "Alchemy API")

AlchemyAPI provide a REST interface which will analyze the provided text and give a result as follows. To sign up for a developer key (necessary for running any of the sample code, you can register [here](http://www.alchemyapi.com/api/register.html).

Let’s use the service analyze this actual tweet by John Carmick about the new Iron Man 3 movie:


<blockquote class="twitter-tweet"><p>Iron Man 3 was fun, but letting the tech genie of autonomously controlled, series produced armor out of the bottle hampers future stories.</p>&mdash; John Carmack (@ID_AA_Carmack) <a href="https://twitter.com/ID_AA_Carmack/status/331232260856639488">May 6, 2013</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>

To do so, we need to a POST to http://access.alchemyapi.com/calls/text/TextGetTextSentiment with the following parametes:

| Parameter | Value                                      |
|-----------|--------------------------------------------|
|apiKey     | the key assigned to use when we registered |
|text       | the text to analyze                        | 
|outputMode | format of the results.  xml or json        |


In this case, we get back the following response:

```javascript
 {
    "status": "OK",
    "language": "english",
    "docSentiment": {
        "type": "positive",
        "score": "0.0434121",
        "mixed": "1"
    }
}
```

As you can see, the API determines the language the text was written in, and then provides an analysis of whether or not the text was overall positive, negative, or neutral. Scores range from -1.0 to +1.0. In addition, if it appears the tweet has both positive and negative aspects, a “mixed” flag with a value of 1 will be provided. Compare that response to this one, for the much more straightforward, negative review:

<blockquote class="twitter-tweet"><p>Wow. I don't mean to be mean but I really didn't enjoy Iron Man 3. Super let down.</p>&mdash; (@therealcliffyb) <a href="https://twitter.com/therealcliffyb/status/331230333494259713">May 6, 2013</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>

results in:

```javascript
{
    "status": "OK",
    "language": "english",
    "docSentiment": {
        "type": "negative",
        "score": "-0.123076"
    }
}
```

As you can see, this service does an admirable job of analyzing the sentiment of these tweets. So now let’s get to some code. Full source code can be found here. The project is using Spring, Spring Data, Hibernate, Jackson, Twitter4j, and is built with Maven.

We’ll do a brief overview of the code, starting with our simple data model.

```java
public enum Mood {
    POSITIVE,  NEUTRAL, NEGATIVE
}

@Embeddable
public class Sentiment {
 
    private Mood mood;
 
    private float confidence;
    
    //implementation omitted
}


/**
* A topic is what users which to search twitter for.
*/
@Entity
public class Topic {
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long topicId;
 
    private String name;
 
}

@Entity
public class Tweet {
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long tweetId;
    
    /** statusId is the unique key assigned by Twitter */
    private Long statusId;
 
    private String text;
 
    private String username;
 
    private Date date;
 
    private Sentiment sentiment;
    
    //implementation omitted
}
```

We’re going to use a library called [Twitter4j](http://twitter4j.org/en/index.html) to stream results from Twitter that match a given Topic. The full code is lengthy is can be found at [TopicListener.java](https://github.com/gcase/twitterSentiment/blob/master/src/main/java/com/sdg/ts/service/TopicListener.java), but this is the part we’re most concerned with:

The [TweetSink](https://github.com/gcase/twitterSentiment/blob/master/src/main/java/com/sdg/ts/service/TweetSink.java) is nothing more than an interface that can consume a Tweet. We happen to have [AnalyzingTweetSink](https://github.com/gcase/twitterSentiment/blob/master/src/main/java/com/sdg/ts/service/AnalyzingTweetSink.java) implementation. It uses an Executor to dispatch the analysis of a Tweet to a [SentimentAnalyzer](https://github.com/gcase/twitterSentiment/blob/master/src/main/java/com/sdg/ts/service/SentimentAnalyzer.java) interface. This code is using a simple single threated Executor, as we don’t want to overwhelm the service just for this demonstration. You can also see that we are using a class called [SentimentStats](https://github.com/gcase/twitterSentiment/blob/master/src/main/java/com/sdg/ts/service/SentimentStats.java), which is just there to aggregate the Sentiments we’ve generated for the tweets, and provide some useful metrics. 
Here is the code doing the Executor will run for each Tweet:

```java
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
```    
As you can see, it checks to see if the Tweet has already been analyzed, just in case our stream hiccups or our server restarts. It then uses our injected SentimentAnalyzer to determine the Sentiment, and finally persists the Tweet. The SentimentAnalyzer implementation itself is [AlchemyApiSentimentAnalyzer](https://github.com/gcase/twitterSentiment/blob/master/src/main/java/com/sdg/ts/service/AlchemyApiSentimentAnalyzer.java). It’s not exactly pretty, but it works.

Now that we’ve run thru the code, let’s stick a couple of topics at it. Here is Iron Man 3:

```
Total: 89
Negative: 11, average confidence: -0.17
Neutral: 46
Positive: 32, average confidence: 0.18
Overall Mood: Positive, confidence: 0.09
```

And here is what it returns for the IRS (Spoiler alert: Most people don't like the IRS)

```
Total: 101
Negative: 59, average confidence: -0.18
Neutral: 15
Positive: 27, average confidence: 0.11
Overall Mood: Negative, confidence: -0.09
```


Now that we have a skeleton of an application, there are a number of different directions we could take it. One can imagine a service where a brand manager could configure a number of topics to monitor. Especially negative messages could trigger a follow up with a Customer Support Specialist. Or it could be used to gauge the effectiveness of a new ad campaign in real time. Topics could be monitored for a sudden upswing or downswing in sentiment, perhaps indicating something has gone viral. Or the service could even be used for purchase decisions, pitting Honda against Toyota and seeing which one has a greater positive sentiment.  



