package com.gt.ssm.key;

import com.gt.ssm.model.KeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class KeyTypeDao {

    private static final Logger log = LoggerFactory.getLogger(KeyTypeDao.class);

    private static final String LOAD_ALL_KEY_TYPES_SQL =
            "SELECT id, name, abbr FROM key_type";

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public KeyTypeDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.template = namedParameterJdbcTemplate;
    }

    public List<KeyType> getAllKeyTypes() {
        return template.query(LOAD_ALL_KEY_TYPES_SQL, (rs, rowNum) -> parseKeyType(rs, rowNum));
    }

    private KeyType parseKeyType(ResultSet rs, int rowNum) throws SQLException {
        return new KeyType(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("abbr")
        );
    }
}
