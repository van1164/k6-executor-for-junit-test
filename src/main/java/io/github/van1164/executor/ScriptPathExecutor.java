/* ────────────────────────────────────────────────────────────────
 *  ScriptPathExecutor.java
 *  Executes a *.js script file
 * ──────────────────────────────────────────────────────────────── */
package io.github.van1164.executor;

import io.github.van1164.result.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        /* 1) 먼저 raw 메트릭 라인들을 Map<String,String> 으로 수집 */
        Pattern pMetric = Pattern.compile(
                "^\\s*[│▕]?\\s*"                                  // 앞 공백 + 그리드문자 허용
                        + "(http_req_duration|http_req_failed|http_reqs|"
                        +  "iteration_duration|iterations|data_received|data_sent)"
                        + "[^.]*[.:]\\s*(.*)$",                            // ▶︎ key...: value 전체
                Pattern.MULTILINE);

        Map<String, String> metricLineMap = new HashMap<>();
        Matcher mMetric = pMetric.matcher(raw);
        while (mMetric.find()) {
            String key   = mMetric.group(1).trim();      // http_req_duration …
            String full  = mMetric.group(0);             // 전체 라인
            String value = full.replaceFirst("^.*?:\\s*", "").trim();
            //          └─ 첫 번째 콜론까지 삭제 →  "avg=36.68ms ..."
            metricLineMap.put(key, value);
        }
        /* 3) 각 메트릭 파싱 */
        DurationStats httpDur = parseDuration(metricLineMap.getOrDefault("http_req_duration", ""));
        CounterStats httpFail = parseCounter(metricLineMap.getOrDefault("http_req_failed", "0 out of 0"));
        CounterStats httpReqs = parseCounter(metricLineMap.getOrDefault("http_reqs", "0 0/s"));
        DurationStats iterDur = parseDuration(metricLineMap.getOrDefault("iteration_duration", ""));
        CounterStats iters = parseCounter(metricLineMap.getOrDefault("iterations", "0 0/s"));
        DataStats dataRecv = parseData(metricLineMap.getOrDefault("data_received", "0 B 0 B/s"));
        DataStats dataSent = parseData(metricLineMap.getOrDefault("data_sent", "0 B 0 B/s"));
        return new K6Result(
                raw,
                allPass,
                failed,
                counters,
                metricLineMap,
                httpDur,
                httpFail,
                httpReqs,
                iterDur,
                iters,
                dataRecv,
                dataSent
        );
    }
}
