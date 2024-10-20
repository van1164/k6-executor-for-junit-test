package io.github.van1164.scirptBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.van1164.util.K6Constants;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class K6ScriptBuilder {
    private final StringBuilder script;
    private final List<String> imports;
    private final List<Check> checks;
    private final List<String> counters;
    private final List<Threshold> thresholds;
    private final List<String> vus;
    private final List<String> durations;
    private List<String> scenarios;
    private final List<String> tags;

    public K6ScriptBuilder() {
        this.script = new StringBuilder();
        this.imports = new ArrayList<>();
        this.checks = new ArrayList<>();
        this.counters = new ArrayList<>();
        this.thresholds = new ArrayList<>();
        this.vus = new ArrayList<>();
        this.durations = new ArrayList<>();
        this.scenarios = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // Import 추가
    public K6ScriptBuilder addImport(String module) {
        this.imports.add(module + ";\n");
        return this;
    }

    // VU 설정
    public K6ScriptBuilder addVU(int vus) {
        this.vus.add("'"+K6Constants.VUS + "': " + vus + ",\n");
        return this;
    }

    // Duration 설정
    public K6ScriptBuilder addDuration(String duration) {
        this.durations.add("'"+K6Constants.DURATION + "': '" + duration + "',\n");
        return this;
    }

    // HttpRequest 추가
    public K6ScriptBuilder addHttpRequest(HttpRequest request) throws JsonProcessingException {
        if (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PUT) {
            String jsonBody = request.getBodyAsJson();
            this.script.append("let payload = '").append(jsonBody).append("';\n");
        }
        this.script.append("let ").append(request.getVariableName()).append(" = http.").append(request.getMethod().toString().toLowerCase()).append("('").append(request.getUrl()).append("');\n");
        return this;
    }

    // Check 추가
    public K6ScriptBuilder addCheck(Check check) {
        this.checks.add(check);
        return this;
    }

    // Counter 추가
    public K6ScriptBuilder addCounter(String name) {
        this.counters.add("var " + name + " = 0;\n");
        return this;
    }

    public K6ScriptBuilder incrementCounter(String name) {
        this.counters.add(name + " += 1;\n");
        return this;
    }

    // Threshold 추가
    public K6ScriptBuilder addThreshold(Threshold threshold) {
        this.thresholds.add(threshold);
        return this;
    }

    // Tag 추가
    public K6ScriptBuilder addTag(String key, String value) {
        this.tags.add(String.format("'tags': { '%s': '%s' }", key, value));
        return this;
    }

    // 빌드 메서드
    public String build() {
        StringBuilder finalScript = new StringBuilder();

        // Imports
        for (String imp : imports) {
            finalScript.append(imp);
        }

        // Options 설정 (VU와 Duration)
        if (!vus.isEmpty() || !durations.isEmpty()) {
            finalScript.append("export let options = {\n");
            for (String vu : vus) {
                finalScript.append(vu);
            }
            for (String duration : durations) {
                finalScript.append(duration);
            }
            finalScript.append("};\n");
        }
        // Thresholds
        if (!thresholds.isEmpty()) {
            finalScript.append("export let thresholds = {\n");
            for (Threshold threshold : thresholds) {
                finalScript.append(threshold).append(",\n");
            }
            finalScript.append("};\n");
        }

        // Main script 시작
        finalScript.append("export default function () {\n");

        // HttpRequests
        finalScript.append(script);

        // Counters
        for (String counter : counters) {
            finalScript.append(counter);
        }

        // Checks
        for (Check check : checks) {
            finalScript.append(check).append("\n");
        }

        finalScript.append("}\n");
        return finalScript.toString();
    }
}