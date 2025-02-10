package com.gt.ssm.secret;

import com.gt.ssm.model.SecretType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SecretTypeDao {

    private static final Logger log = LoggerFactory.getLogger(SecretTypeDao.class);

    private static final String GET_SECRET_TYPE_BY_ID =
            "SELECT id, name, abbr FROM secret_type WHERE id = :id";
    private static final String GET_ALL_SECRET_TYPES_QUERY =
            "SELECT id, name, abbr FROM secret_type";

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public SecretTypeDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.template = namedParameterJdbcTemplate;
    }

    public SecretType getSecretTypeById(String id) {
        return template.queryForObject(GET_SECRET_TYPE_BY_ID, Map.of("id", id), (rs, rowNum) -> new SecretType(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("abbr")));
    }

    public List<SecretType> getAllSecretTypes() {
        return template.query(GET_ALL_SECRET_TYPES_QUERY,
                (rs, rowNum) -> new SecretType(
                   rs.getString("id"),
                   rs.getString("name"),
                   rs.getString("abbr")));
    }
}
