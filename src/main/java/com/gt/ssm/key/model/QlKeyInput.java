package com.gt.ssm.key.model;

import java.util.HashMap;
import java.util.Map;

public record QlKeyInput(String id, String name, String comments, String typeId, String keyPassword, String imageName) {

    public Map<String, String> toMap() {
        Map<String, String> values = new HashMap<>();

        if (id != null) {
            values.put("id", id);
        }
        if (name != null) {
            values.put("name", name);
        }
        if (comments != null) {
            values.put("comments", comments);
        }
        if (typeId != null) {
            values.put("typeId", typeId);
        }
        if (keyPassword != null) {
            values.put("keyPassword", keyPassword);
        }
        if (imageName != null) {
            values.put("imageName", imageName);
        }

        return values;
    }

}