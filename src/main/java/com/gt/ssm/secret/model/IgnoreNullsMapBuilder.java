package com.gt.ssm.secret.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IgnoreNullsMapBuilder<K, V>  {

    private Map<K, V> tempMap;

    public IgnoreNullsMapBuilder() {
        tempMap = new HashMap<>();
    }

    public IgnoreNullsMapBuilder withEntry(K key, V value) {
        if (key != null && value != null) {
            tempMap.put(key, value);
        }

        return this;
    }

    public Map<K, V> Build() {
        return Collections.unmodifiableMap(tempMap);
    }
}
