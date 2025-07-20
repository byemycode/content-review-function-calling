package com.xiaofuge.functioncalling;

import java.util.Map;

@FunctionalInterface
public interface FunctionHandler {
    Object handle(Map<String, Object> arguments) throws Exception;
}