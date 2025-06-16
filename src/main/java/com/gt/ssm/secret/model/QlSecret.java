package com.gt.ssm.secret.model;

import com.gt.ssm.key.model.QlKey;
import com.gt.ssm.model.SecretType;

public record QlSecret(String id,
                       String imageName,
                       String name,
                       String comments,
                       SecretType type,
                       QlKey key,
                       QlWebsitePasswordComponents websitePasswordComponents,
                       QlCreditCardComponents creditCardComponents,
                       QlTextBlobComponents textBlobComponents,
                       QlFilesComponents filesComponents) { }
