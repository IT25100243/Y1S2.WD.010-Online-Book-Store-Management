package com.pageturner.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseEntity {

    // ENCAPSULATION: private fields — only accessible via getters
    private final String id;
    private final String createdAt;

    protected static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected BaseEntity(String id) {
        this.id        = id;
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }

    // ENCAPSULATION: read-only getters 
    public String getId()        { return id; }
    public String getCreatedAt() { return createdAt; }

    // ABSTRACTION: subclasses must define how they serialise to JSON
    public abstract String toJson();

    // ABSTRACTION: subclasses must declare their entity type
    public abstract String getEntityType();

    // POLYMORPHISM: each subclass gives a meaningful string
    @Override
    public String toString() {
        return "[" + getEntityType() + " id=" + id + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        return id.equals(((BaseEntity) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
