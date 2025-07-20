package com.xiaofuge.functioncalling;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDefinition {
    private String name;
    private String description;
    private Map<String, Object> parameters;
    private String returnType;
}