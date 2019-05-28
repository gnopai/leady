package com.gnopai.leady;

import lombok.Value;

import java.util.Objects;
import java.util.Set;

@Value
public class LeadChange {
    String originalId;
    String originalEmail;
    Set<FieldDiff> fieldDiffs;

    @Value
    public static class FieldDiff {
        String fieldName;
        Object oldValue;
        Object newValue;

        public boolean hasChanged() {
            return !Objects.equals(oldValue, newValue);
        }
    }
}
