package com.sdg.ts.service;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sdg.ts.model.Sentiment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class SentimentStats {

    private List<Sentiment> positives = Lists.newArrayList();
    private List<Sentiment> negatives = Lists.newArrayList();
    private List<Sentiment> neutrals = Lists.newArrayList();


    public void add(Sentiment sentiment) {
        switch(sentiment.getMood()) {
            case NEGATIVE:
                negatives.add(sentiment);
                break;
            case NEUTRAL:
                neutrals.add(sentiment);
                break;
            case POSITIVE:
                positives.add(sentiment);
                break;
        }
    }

    private Optional<BigDecimal> average(Iterable<Sentiment> sentiments) {
        if (Iterables.isEmpty(sentiments))     {
           return Optional.absent();
       }

      BigDecimal sum = new BigDecimal(0);
      int count = 0;
      for (Sentiment sentiment: sentiments) {
          sum = sum.add(new BigDecimal(sentiment.getConfidence()));
          count++;
      }
      return Optional.of(sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_DOWN));
    }

    public String toString() {
        String avgPositive = formatAverage(average(positives));
        String avgNegative = formatAverage(average(negatives));
        Optional<BigDecimal> overallAverage = average(Iterables.concat(positives, negatives));

        StringBuilder builder = new StringBuilder();
        builder.append("Total: ").append(negatives.size() + neutrals.size() + positives.size()).append("\n");
        builder.append("Negative: ").append(negatives.size()).append(", average confidence: ").append(avgNegative).append("\n");
        builder.append("Neutral: ").append(neutrals.size()).append("\n");
        builder.append("Positive: ").append(positives.size()).append(", average confidence: ").append(avgPositive).append("\n");
        if (overallAverage.isPresent()) {
            String averageMood = overallAverage.get().floatValue() > 0 ? "Positive" : "Negative";
            builder.append("Overall Mood: " + averageMood + ", confidence: " + formatAverage(overallAverage));
        }

        return builder.toString();
    }

    private String formatAverage(Optional<BigDecimal> value) {
        if (value.isPresent()) {
            return new DecimalFormat("#0.##").format(value.get());
        } else {
            return "N/A";
        }
    }

}
