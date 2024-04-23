package com.windowforsun.kafka.retrytopic.util;

import com.windowforsun.kafka.retrytopic.event.CreateItem;
import com.windowforsun.kafka.retrytopic.event.UpdateItem;
import com.windowforsun.kafka.retrytopic.service.ItemStatus;

import java.util.UUID;

public class TestEventData {
    public static CreateItem buildCreateItemEvent(UUID id, String name) {
        return CreateItem.builder()
                .id(id)
                .name(name)
                .build();
    }

    public static UpdateItem buildUpdateItemEvent(UUID id, ItemStatus status) {
        return UpdateItem.builder()
                .id(id)
                .status(status)
                .build();
    }
}
