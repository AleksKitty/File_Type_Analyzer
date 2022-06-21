package analyzer;

public class Pattern {

    private int priority;
    private String pattern;
    private String result;

    public Pattern(int priority, String pattern, String result) {
        this.priority = priority;
        this.pattern = pattern;
        this.result = result;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
