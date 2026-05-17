package com.markbaengine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SchemaValidationResult {
    private final boolean valid;
    private final List<String> messages;

    public SchemaValidationResult(boolean valid, List<String> messages) {
        this.valid = valid;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String toDisplayText() {
        if (messages.isEmpty()) {
            return valid ? "Valid Urban Fleet SQL file." : "Invalid SQL file.";
        }
        return String.join(System.lineSeparator(), messages);
    }
}
