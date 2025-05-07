package io.github.van1164.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class DataStats {
    private final long   bytes;  // absolute bytes
    private final double rate;   // bytes per second
}