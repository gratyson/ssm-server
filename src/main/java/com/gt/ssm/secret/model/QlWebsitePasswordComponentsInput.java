package com.gt.ssm.secret.model;

import java.util.Map;

public record QlWebsitePasswordComponentsInput(QlComponentInput website, QlComponentInput username, QlComponentInput password) implements QlComponentsInput {
    @Override
    public Map<String, QlComponentInput> toComponentTypeInputs() {
        return new IgnoreNullsMapBuilder()
                .withEntry("website", website)
                .withEntry("username", username)
                .withEntry("password", password)
                .Build();
    }
}

