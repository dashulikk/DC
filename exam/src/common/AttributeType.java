package common;

import java.io.Serializable;

public enum AttributeType implements Serializable {
    TITLE("title"),
    AUTHOR("author");

    private final String value;

    AttributeType(String value) {
        this.value = value;
    }
}
