package io.symphonia.shared;


import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class AttributeUtil {

    public static String getOrDefaultS(Map<String, AttributeValue> item, String key, String def) {
        return getOrDefaultS(item.get(key), def);
    }

    public static String getOrDefaultS(AttributeValue attributeValue, String def) {
        return attributeValue == null ? def : attributeValue.s();
    }

    public static String getOrThrowS(Map<String, AttributeValue> item, String key) {
        if (!item.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Missing key: %s", key));
        } else {
            return item.get(key).s();
        }
    }

    public static long getOrDefaultL(Map<String, AttributeValue> item, String key, long def) {
        return getOrDefaultL(item.get(key), def);
    }

    public static long getOrDefaultL(AttributeValue attributeValue, long def) {
        return attributeValue == null ? def : Long.parseLong(attributeValue.n());
    }

    public static long getOrThrowL(Map<String, AttributeValue> item, String key) {
        if (!item.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Missing key: %s", key));
        } else {
            return Long.parseLong(item.get(key).n());
        }
    }

}
