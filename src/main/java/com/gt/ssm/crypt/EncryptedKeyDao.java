package com.gt.ssm.crypt;

import com.gt.ssm.crypt.model.EncryptedKey;
import com.gt.ssm.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EncryptedKeyDao {

    private static final Logger log = LoggerFactory.getLogger(EncryptedKeyDao.class);

    private static final String GET_USER_DEFAULT_ENCRYPTED_KEY_SQL =
            "SELECT encrypted_key, encryption_algorithm, iv " +
            "FROM encrypted_keys " +
            "WHERE owner = :owner AND user_key_id IS NULL ";
    private static final String GET_ENCRYPTED_KEY_SQL =
            "SELECT encrypted_key, encryption_algorithm, iv " +
            "FROM encrypted_keys " +
            "WHERE owner = :owner and user_key_id = :userKeyId ";
    private static final String SET_ENCRYPTED_KEY_SQL =
            "INSERT INTO encrypted_keys (owner, user_key_id, encrypted_key, encryption_algorithm, iv) " +
                    "VALUES (:owner, :userKeyId, :encryptedKey, :encryptionAlgorithm, :iv) ";

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public EncryptedKeyDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.template = namedParameterJdbcTemplate;
    }

    public EncryptedKey getUserDefaultEncryptedKey(String owner) {
        return queryForEncryptedKey(GET_USER_DEFAULT_ENCRYPTED_KEY_SQL, owner, null);
    }

    public void saveUserDefaultEncryptedKey(String owner, EncryptedKey encryptedKey) {
        saveEncryptedKey(owner, null, encryptedKey);
    }

    public EncryptedKey getEncryptedKey(String owner, String userKeyId) {
        return queryForEncryptedKey(GET_ENCRYPTED_KEY_SQL, owner, userKeyId);
    }

    private EncryptedKey queryForEncryptedKey(String sql, String owner, String userKeyId) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("owner", owner);
        source.addValue("userKeyId", userKeyId);

        List<EncryptedKey> encryptedKeys = template.query(sql, source, (rs, rowNum) ->
                new EncryptedKey(rs.getString("encrypted_key"), rs.getString("encryption_algorithm"), rs.getString("iv")));

        if (encryptedKeys != null && encryptedKeys.size() > 0) {
            if (encryptedKeys.size() > 1) {
                log.error("Expected 1 encrypted key but found multiple for keyId=" + userKeyId);
                throw new DaoException("Data integrity issue in encrypted keys");
            }

            return encryptedKeys.get(0);
        }

        return null;
    }

    public void saveEncryptedKey(String owner, String userKeyId, EncryptedKey encryptedKey) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("owner", owner);
        source.addValue("userKeyId", userKeyId);
        source.addValue("encryptedKey", encryptedKey.key());
        source.addValue("encryptionAlgorithm", encryptedKey.encryptionAlgorithm());
        source.addValue("iv", encryptedKey.iv());

        template.update(SET_ENCRYPTED_KEY_SQL, source);
    }
}
