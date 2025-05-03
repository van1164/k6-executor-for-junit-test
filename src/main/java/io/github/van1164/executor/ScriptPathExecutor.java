/* ────────────────────────────────────────────────────────────────
 *  ScriptPathExecutor.java
 *  Executes a *.js script file
 * ──────────────────────────────────────────────────────────────── */
package io.github.van1164.executor;

import io.github.van1164.result.HttpReq;
import io.github.van1164.result.K6Result;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.*;

import static io.github.van1164.util.K6RegexFinder.*;

@Getter
@SuperBuilder
public final class ScriptPathExecutor extends K6Executor {

    @NonNull private final String scriptPath;
    @Builder.Default private final List<String> checkList   = List.of();
    @Builder.Default private final List<String> counterList = List.of();
    @Builder.Default private final Map<String,String> args  = Map.of();

    @Override protected String[] createCommand() {
        List<String> cmd = new ArrayList<>(List.of(k6BinaryPath, "run", scriptPath));
        args.forEach((k,v) -> Collections.addAll(cmd, "--env", k + '=' + v));
        return cmd.toArray(String[]::new);
    }

    @Override protected K6Result resultToK6Result(String raw) {
        HashMap<String,Integer> counters = new HashMap<>();
        counterList.forEach(c -> counters.put(c, countByResult(raw, c)));

        List<String> failed = new ArrayList<>();
        boolean allPass = checkList.stream().allMatch(ch -> {
            boolean ok = raw.contains("✓ " + ch);
            if (!ok) failed.add(ch);
            return ok;
        });

        return new K6Result(
                raw,
                allPass,
                failed,
                countHttpReq(raw),
                counters
        );
    }
}
