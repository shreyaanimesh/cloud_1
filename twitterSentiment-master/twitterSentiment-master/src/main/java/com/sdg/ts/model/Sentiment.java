package com.sdg.ts.model;

import com.google.common.base.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class Sentiment {

    private Mood mood;
    private float confidence;

    public Sentiment() {
    }

    public Sentiment(Mood mood, float confidence) {
        this.mood = mood;
        this.confidence = confidence;
    }

    @Enumerated(EnumType.STRING)
    public Mood getMood() {
        return mood;
    }

    public float getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mood", mood)
                .add("confidence", confidence)
                .toString();
    }

    public static Sentiment neutral() {
       return new Sentiment(Mood.NEUTRAL, 0f);
    }
}
