package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponentType;
import graphql.com.google.common.collect.ImmutableMap;

public record QlFilesComponentsFileInput(QlComponentInput fileId, QlComponentInput fileName) implements QlComponentsInput {
    @Override
    public QlComponentInputs toComponentTypeInputs() {
        return new QlComponentInputs(
                new IgnoreNullsMapBuilder()
                    .withEntry(SecretComponentType.FILE_ID, fileId)
                    .withEntry(SecretComponentType.FILE_NAME, fileName)
                    .Build(),
                ImmutableMap.of());
    }
}
