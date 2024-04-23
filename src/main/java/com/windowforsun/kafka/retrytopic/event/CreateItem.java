package com.windowforsun.kafka.retrytopic.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItem {
    @NotNull
    private UUID id;
    @NotNull
    private String name;
}
