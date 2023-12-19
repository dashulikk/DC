package common;

import java.io.Serializable;

public enum OperationType implements Serializable {
    LINKED_PUBLICATIONS("Linked Publications"),
    TITLE_PUBLICATION("Title Publication"),
    AUTHOR_PUBLICATION("Author Publication"),
    CLOSE("close")
    ;

    private final String value;

    OperationType(String value) {
        this.value = value;
    }

}
