package com.dmlogs.examples.models;

import java.util.UUID;

/**
 * Created by dlogan on 9/26/16.
 */
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
