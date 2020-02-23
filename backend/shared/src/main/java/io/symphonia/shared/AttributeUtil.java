package io.symphonia.shared;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Map;

public class AttributeUtil {

    public static String getOrDefaultS(Map<String, AttributeValue> item, String key, String def) {
        return getOrDefaultS(item.get(key), def);
    }

    public static String getOrDefaultS(AttributeValue attributeValue, String def) {
        return attributeValue == null ? def : attributeValue.getS();
    }

    public static String getOrThrowS(Map<String, AttributeValue> item, String key) {
        if (!item.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Missing key: %s", key));
        } else {
            return item.get(key).getS();
        }
    }

    public static long getOrDefaultL(Map<String, AttributeValue> item, String key, long def) {
        return getOrDefaultL(item.get(key), def);
    }

    public static long getOrDefaultL(AttributeValue attributeValue, long def) {
        return attributeValue == null ? def : Long.parseLong(attributeValue.getN());
    }

    public static long getOrThrowL(Map<String, AttributeValue> item, String key) {
        if (!item.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Missing key: %s", key));
        } else {
            return Long.parseLong(item.get(key).getN());
        }
    }

}
