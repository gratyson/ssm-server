package com.gt.ssm.secret.model;

import java.util.Map;

public record QlTextBlobComponentsInput(QlComponentInput textBlob) implements QlComponentsInput {
    @Override
    public Map<String, QlComponentInput> toComponentTypeInputs() {
        return new IgnoreNullsMapBuilder()
                .withEntry("text_blob", textBlob)
                .Build();
    }
}
