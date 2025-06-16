package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponentType;
import graphql.com.google.common.collect.ImmutableMap;

import java.util.Map;

public record QlWebsitePasswordComponentsInput(QlComponentInput website, QlComponentInput username, QlComponentInput password) implements QlComponentsInput {

    @Override
    public QlComponentInputs toComponentTypeInputs() {
        return new QlComponentInputs(
                new IgnoreNullsMapBuilder()
                        .withEntry(SecretComponentType.WEBSITE, website)
                        .withEntry(SecretComponentType.USERNAME, username)
                        .withEntry(SecretComponentType.PASSWORD, password)
                        .Build(),
                ImmutableMap.of());
    }
}

