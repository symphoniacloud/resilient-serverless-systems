package io.symphonia.lambda;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.stream.Collectors;

public class AttributeValueConverter {

    public static AttributeValue oldToNew(com.amazonaws.services.dynamodbv2.model.AttributeValue old) {
        var builder = AttributeValue.builder();

        if (old.getS() != null) {
            builder.s(old.getS());
        } else if (old.getN() != null) {
            builder.n(old.getN());
        } else if (old.getBOOL() != null) {
            builder.bool(old.getBOOL());
        } else if (old.getB() != null) {
            builder.b(SdkBytes.fromByteBuffer(old.getB()));
        } else if (old.getNULL() != null) {
            builder.nul(old.getNULL());
        } else if (old.getSS() != null) {
            builder.ss(old.getSS());
        } else if (old.getNS() != null) {
            builder.ns(old.getNS());
        } else if (old.getBS() != null) {
            builder.bs(old.getBS().stream().map(SdkBytes::fromByteBuffer).collect(Collectors.toList()));
        } else if (old.getL() != null) {
            builder.l(old.getL().stream().map(AttributeValueConverter::oldToNew).collect(Collectors.toList()));
        } else if (old.getM() != null) {
            builder.m(oldToNew(old.getM()));
        }

        return builder.build();
    }

    public static Map<String, AttributeValue> oldToNew(Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> old) {
        return old.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> oldToNew(e.getValue())));
    }

}
