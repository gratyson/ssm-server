package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponentType;

import java.util.List;
import java.util.stream.Collectors;

public record  QlFilesComponentsInput(List<QlFilesComponentsFileInput> files) implements QlComponentsInput {
    @Override
    public QlComponentInputs toComponentTypeInputs() {
        return new QlComponentInputs(
                new IgnoreNullsMapBuilder().Build(),
                new IgnoreNullsMapBuilder()
                        .withEntry(SecretComponentType.FILE_ID, files.stream().map(file -> file.fileId()).collect(Collectors.toList()).toArray(new QlComponentInput[0]))
                        .withEntry(SecretComponentType.FILE_NAME, files.stream().map(file -> file.fileName()).collect(Collectors.toList()).toArray(new QlComponentInput[0]))
                        .Build());
    }
}
