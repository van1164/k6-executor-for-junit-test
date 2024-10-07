package io.github.van1164.scirptBuilder;

public class Threshold {
    private String metric;
    private int percentile;
    private int maxDuration;

    public Threshold(String metric, int percentile, int maxDuration) {
        this.metric = metric;
        this.percentile = percentile;
        this.maxDuration = maxDuration;
    }

    @Override
    public String toString() {
        return String.format("'%s': ['p(%d) < %d']", metric, percentile, maxDuration);
    }
}