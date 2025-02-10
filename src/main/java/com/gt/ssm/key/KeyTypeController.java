package com.gt.ssm.key;

import com.gt.ssm.model.KeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class KeyTypeController {

    private static final Logger log = LoggerFactory.getLogger(KeyTypeController.class);

    private final KeyTypeService keyTypeService;

    @Autowired
    public KeyTypeController(KeyTypeService keyTypeService) {
        this.keyTypeService = keyTypeService;
    }

    @QueryMapping
    public List<KeyType> keyTypes() {
        return keyTypeService.getAllKeyTypes();
    }
}
