package com.gt.ssm.secret.model;

import java.util.Map;

public interface QlComponentsInput {

    QlComponentInputs toComponentTypeInputs();

    record QlComponentInputs(Map<String, QlComponentInput> scalarComponents, Map<String, QlComponentInput[]> arrayComponents) { }
}
