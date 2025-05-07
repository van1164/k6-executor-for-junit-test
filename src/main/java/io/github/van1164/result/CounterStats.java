package io.github.van1164.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CounterStats {
    private final long   total;
    private final double rate;   // per‑second
}