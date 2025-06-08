package at.jku.dke.task_app.clustering.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterSyntaxValidator {

    private static final String CLUSTER = "\\[\\(\\d+(\\.\\d{1,2})?,\\d+(\\.\\d{1,2})?\\):[A-Z](,\\s*[A-Z])*\\]";
    private static final String CLUSTER_GROUP = CLUSTER + "(;\\s*" + CLUSTER + ")*";
    private static final String ITERATION = CLUSTER_GROUP + "(\\s*--\\s*" + CLUSTER_GROUP + ")*";
    private static final Pattern pattern = Pattern.compile("^" + ITERATION + "$");
    public static final Pattern clusterGroupPattern = Pattern.compile("^" + CLUSTER_GROUP + "$");
    public static boolean isValidSyntax(String input) {
        String cleanedInput = input.replaceAll("\\s+", "");
        return pattern.matcher(cleanedInput.trim()).matches();
    }
    public static boolean isValidClusterGroup(String input) {
        String cleaned = input.replaceAll("\\s+", "");
        return clusterGroupPattern.matcher(cleaned).matches();
    }
    public static int indexOfLastMatch(String input) {
        Matcher matcher = pattern.matcher(input);
        for (int i = input.length(); i > 0; --i) {
            matcher.region(0, i);
            if (matcher.matches() || matcher.hitEnd()) {
                return i;
            }
        }
        return 0;
    }
}
