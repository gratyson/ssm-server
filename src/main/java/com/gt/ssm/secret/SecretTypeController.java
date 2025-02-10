package com.gt.ssm.secret;

import com.gt.ssm.model.SecretType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SecretTypeController {

    private final SecretTypeService secretTypeService;

    @Autowired
    public SecretTypeController(SecretTypeService secretTypeService) {
        this.secretTypeService = secretTypeService;
    }

    @QueryMapping
    public SecretType secretType(@Argument String id) {
        return secretTypeService.getSecretTypeById(id);
    }

    @QueryMapping
    public List<SecretType> secretTypes() {
        return secretTypeService.getAllSecretTypes();
    }
}
