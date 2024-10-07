package io.github.van1164.scirptBuilder;

public class Check {
    private String variable;
    private String description;
    private String condition;

    public Check(String variable, String description, String condition) {
        this.variable = variable;
        this.description = description;
        this.condition = condition;
    }

    // 자주 사용하는 체크 메서드 (예: status is 200)
    public static Check statusCheck(String variable, int statusCode) {
        return new Check(variable, "status is " + statusCode, String.format("r => r.status === %d", statusCode));
    }

    @Override
    public String toString() {
        return String.format("check(%s, { '%s': %s });", variable, description, condition);
    }
}