package ru.max.module_two.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Claim {
    private String name;
    private String type;
    private Double amount;
}
