package com.windowforsun.kafka.retrytopic.util;

import com.windowforsun.kafka.retrytopic.domain.Item;
import com.windowforsun.kafka.retrytopic.service.ItemStatus;

import java.util.UUID;

public class TestEntityData {
    public static Item buildItem(UUID id, String name) {
        return Item.builder()
                .id(id)
                .name(name)
                .status(ItemStatus.ACTIVE)
                .build();
    }
}
