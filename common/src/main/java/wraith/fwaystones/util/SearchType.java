package wraith.fwaystones.util;

public enum SearchType {
    CONTAINS,
    CONTAINS_SUBSEQUENCE,
    STARTS_WITH;

    public boolean match(String string, String filter) {
        return switch (this) {
            case CONTAINS -> string.contains(filter);
            case CONTAINS_SUBSEQUENCE -> Utils.isSubSequence(string, filter);
            case STARTS_WITH -> string.startsWith(filter);
        };
    }
}
