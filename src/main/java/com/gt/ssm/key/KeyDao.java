package com.gt.ssm.key;

import com.gt.ssm.model.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class KeyDao {

    private static final Logger log = LoggerFactory.getLogger(KeyDao.class);

    private static final String LOAD_KEY_SQL =
            "SELECT id, owner, name, comments, type_id, key_password, salt, algorithm, image_name " +
            "FROM keys " +
            "WHERE id = :id AND owner = :owner ";
    private static final String LOAD_OWNED_KEYS_SQL =
            "SELECT id, owner, name, comments, type_id, key_password, salt, algorithm, image_name " +
            "FROM keys " +
            "WHERE owner = :owner ";

    private static final String SAVE_KEY_SQL =
            "INSERT INTO keys (id, owner, name, comments, type_id, key_password, salt, algorithm, image_name) " +
                "VALUES (:id, :owner, :name, :comments, :typeId, :keyPassword, :salt, :algorithm, :imageName) " +
            "ON CONFLICT (id) DO NOTHING ";
    private static final String UPDATE_KEY_SQL_PREFIX =
            "UPDATE keys SET ";
    private final String UPDATE_KEY_SQL_SUFFIX =
            " WHERE id = :id and owner = :owner ";
    private static final Map<String, String> UPDATE_STATEMENTS_BY_COLUMN = Map.of(
            "name", "name = :name",
            "comments", "comments = :comments",
            "typeId", "type_id = :typeId",
            "keyPassword" , "key_password = :keyPassword",
            "salt", "salt = :salt",
            "algorithm", "algorithm = :algorithm",
            "imageName", "image_name = :imageName");
    private static final String DELETE_KEY_SQL =
            "DELETE FROM encrypted_keys WHERE user_key_id = :id AND owner = :owner; " +
            "DELETE FROM keys WHERE id = :id AND owner = :owner; ";

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public KeyDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.template = namedParameterJdbcTemplate;
    }

    public int saveKey(Key key) {
        MapSqlParameterSource keySource = new MapSqlParameterSource();
        keySource.addValue("id", key.id());
        keySource.addValue("owner", key.owner());
        keySource.addValue("name", key.name());
        keySource.addValue("comments", key.comments());
        keySource.addValue("typeId", key.typeId());
        keySource.addValue("keyPassword", key.keyPassword());
        keySource.addValue("salt", key.salt());
        keySource.addValue("algorithm", key.algorithm());
        keySource.addValue("imageName", key.imageName());

        return template.update(SAVE_KEY_SQL, keySource);
    }

    public int updateKey(String id, String owner, Map<String, String> updatedValues) {
        MapSqlParameterSource keySource = new MapSqlParameterSource();
        String setStatement = "";

        for (String keyValue : UPDATE_STATEMENTS_BY_COLUMN.keySet()) {
            if (updatedValues.containsKey(keyValue)) {
                setStatement = (setStatement.equals("") ? "" : setStatement + ", ") + UPDATE_STATEMENTS_BY_COLUMN.get((keyValue));
                keySource.addValue(keyValue, updatedValues.get(keyValue));
            }
        }

        if (setStatement.equals("")) {
            return 0;
        }

        keySource.addValue("id", id);
        keySource.addValue("owner", owner);
        return template.update(UPDATE_KEY_SQL_PREFIX + setStatement + UPDATE_KEY_SQL_SUFFIX, keySource);
    }

    public Key loadKey(String id, String owner) {
        List<Key> keys = template.query(LOAD_KEY_SQL, Map.of("id", id, "owner", owner), this::parseKey);

        if (keys != null && keys.size() > 0) {
            return keys.get(0);
        }

        return null;
    }

    public List<Key> loadOwnedKeys(String owner) {
        return template.query(LOAD_OWNED_KEYS_SQL, Map.of("owner", owner), this::parseKey);
    }

    public void deleteKey(String id, String owner) {
        template.update(DELETE_KEY_SQL, Map.of("id", id, "owner", owner));
    }

    private Key parseKey(ResultSet rs, int rowNum) throws SQLException {
        return new Key(
                rs.getString("id"),
                rs.getString("owner"),
                rs.getString("name"),
                rs.getString("comments"),
                rs.getString("type_id"),
                rs.getString("key_password"),
                rs.getString("salt"),
                rs.getString("algorithm"),
                rs.getString("image_name"));
    }

}
