package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponent;
import com.gt.ssm.model.SecretComponentType;

import java.util.HashMap;
import java.util.Map;

public class QlFilesComponents extends QlSecretComponents {

    private final QlFilesComponentsFile[] files;

    public QlFilesComponents(QlFilesComponentsFile[] files) {
        this.files = files;
    }

    public static QlFilesComponents fromSecretComponents(Iterable<SecretComponent> secretComponents) {
        Map<Integer, QlSecretComponent> idComponents = new HashMap<>();
        Map<Integer, QlSecretComponent> nameComponents = new HashMap<>();
        int maxLineNumber = -1;

        for (SecretComponent secretComponent : secretComponents) {
            if (secretComponent.componentType().equals(SecretComponentType.FILE_ID)) {
                int lineNumber = secretComponent.line();
                maxLineNumber = Math.max(maxLineNumber, lineNumber);

                idComponents.put(lineNumber, toQlSecretComponent(secretComponent));
            } else if (secretComponent.componentType().equals(SecretComponentType.FILE_NAME)) {
                nameComponents.put(secretComponent.line(), toQlSecretComponent(secretComponent));
            }
        }

        if (maxLineNumber < 0) {
            return null;
        }

        QlFilesComponentsFile[] files = new QlFilesComponentsFile[maxLineNumber + 1];

        for (int line = 0; line <= maxLineNumber; line++) {
            QlSecretComponent idComponent = idComponents.get(line);
            if (idComponent != null) {
                files[line] = new QlFilesComponentsFile(idComponent, nameComponents.get(line));
            }
        }

        return new QlFilesComponents(files);
    }

    public QlFilesComponentsFile[] getFiles() {
        return files;
    }
}
