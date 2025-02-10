package com.gt.ssm.secret;

import com.gt.ssm.model.SecretComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SecretComponentTypeDao {
    private static final Logger log = LoggerFactory.getLogger(SecretDao.class);

    private final NamedParameterJdbcTemplate template;

    private static final String GET_ALL_SECRET_COMPONENT_TYPES_SQL =
            "SELECT id, name, encrypted, ql_name " +
            "FROM component_type ";

    private static final String GET_VALID_COMPONENT_TYPES_BY_SECRET_TYPE_SQL =
            "SELECT secret_type_id, component_type_id " +
            "FROM valid_components ";

    @Autowired
    public SecretComponentTypeDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.template = namedParameterJdbcTemplate;
    }

    public List<SecretComponentType> getAllSecretComponentTypes() {
        return template.query(GET_ALL_SECRET_COMPONENT_TYPES_SQL, (rs, rowNum) -> {
            return new SecretComponentType(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getBoolean("encrypted"),
                    rs.getString("ql_name"));
        });
    }
}
