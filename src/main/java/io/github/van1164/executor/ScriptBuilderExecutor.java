/* ────────────────────────────────────────────────────────────────
 *  ScriptBuilderExecutor.java
 *  Executes an in‑memory K6ScriptBuilder via STDIN
 * ──────────────────────────────────────────────────────────────── */
package io.github.van1164.executor;

import io.github.van1164.result.HttpReq;
import io.github.van1164.result.K6Result;
import io.github.van1164.scirptBuilder.Check;
import io.github.van1164.scirptBuilder.K6ScriptBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static io.github.van1164.util.K6RegexFinder.*;

@Getter
@SuperBuilder
public final class ScriptBuilderExecutor extends K6Executor {

    @NonNull private final K6ScriptBuilder sb;

    @Override protected void afterProcessStart(Process p) throws IOException {
        try (OutputStream os = p.getOutputStream()) {
            os.write(sb.build().getBytes());
            os.flush();
        }
    }

    @Override protected String[] createCommand() {
        return new String[] { k6BinaryPath, "run", "-" };
    }

    @Override protected K6Result resultToK6Result(String raw) {
        HashMap<String,Integer> counters = new HashMap<>();
        for (Check c : sb.getChecks()) {
            counters.put(c.getVariable(), countByResult(raw, c.getVariable()));
        }

        List<String> failed = new ArrayList<>();
        boolean allPass = sb.getChecks().stream().allMatch(ch -> {
            boolean ok = raw.contains("✓ " + ch.getVariable());
            if (!ok) failed.add(ch.getVariable());
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
