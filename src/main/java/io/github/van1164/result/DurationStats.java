package io.github.van1164.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class DurationStats {
    private final double avg;
    private final double min;
    private final double med;
    private final double max;
    private final double p90;
    private final double p95;
}
