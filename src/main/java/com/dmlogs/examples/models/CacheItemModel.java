package com.dmlogs.examples.models;

import java.util.UUID;

public class CacheItemModel {
    private UUID id = UUID.randomUUID();
    private long createdAt = System.currentTimeMillis();

    public UUID getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
