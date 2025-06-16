package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponentType;
import graphql.com.google.common.collect.ImmutableMap;

public record QlTextBlobComponentsInput(QlComponentInput textBlob) implements QlComponentsInput {
    @Override
    public QlComponentInputs toComponentTypeInputs() {
        return new QlComponentInputs(
            new IgnoreNullsMapBuilder()
                .withEntry(SecretComponentType.TEXT_BLOB, textBlob)
                .Build(),
                ImmutableMap.of());
    }
}
