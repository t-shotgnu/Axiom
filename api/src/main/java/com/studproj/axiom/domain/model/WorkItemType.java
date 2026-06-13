package com.studproj.axiom.domain.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum WorkItemType {
    Epic, Feature, UserStory, Task, Bug, Subtask;

    /** Returns the hierarchy level of this type (lower = higher in the tree). */
    public int level() {
        switch (this) {
            case Epic:     return 1;
            case Feature:  return 2;
            case UserStory: return 3;
            case Task:
            case Bug:      return 4;
            case Subtask:  return 5;
            default: throw new IllegalStateException("Unknown WorkItemType: " + this);
        }
    }

    /**
     * Returns the work item types that are allowed as direct children of this type.
     * Rules:
     *   - A child must have a strictly higher level than its parent.
     *   - Exception: Subtask may be a child of Subtask (same level, allows deep nesting).
     */
    public List<WorkItemType> allowedChildTypes() {
        return Arrays.stream(WorkItemType.values())
                .filter(candidate -> {
                    if (this == Subtask && candidate == Subtask) return true;
                    return candidate.level() > this.level();
                })
                .collect(Collectors.toList());
    }
}
