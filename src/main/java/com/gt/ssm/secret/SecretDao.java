package com.gt.ssm.secret;

import com.gt.ssm.model.Secret;
import com.gt.ssm.model.SecretComponent;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Integers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class SecretDao {

    private static final Logger log = LoggerFactory.getLogger(SecretDao.class);

    private static final String GET_SECRET_NO_COMPONENTS_SQL =
            "SELECT id, image_name, secret_type, name, comments, key_id, '' as c_id " +
            "FROM secrets s " +
            "WHERE owner = :owner ";

    private static final String GET_SECRET_WITH_COMPONENTS_QUERY_PREFIX_SQL =
            "SELECT s.id, s.image_name, s.secret_type, s.name, s.comments, s.key_id, " +
                   "c.id as c_id, c.component_type, c.value, c.encrypted, c.encryption_algorithm " +
                "FROM secrets s LEFT JOIN " +
                    "(";
    private static final String GET_SECRET_WITH_COMPONENTS_QUERY_SUFFIX_SQL =
                     ") c " +
                "ON s.id = c.secret_id " +
                "WHERE owner = :owner ";
    private static final String GET_SECRET_QUERY_COMPONENT_ID_MATCH_SQL =
            "AND s.id = :id ";
    private static final String GET_SECRET_QUERY_COMPONENT_ID_ORDERING_SQL =
            "ORDER BY s.id ";

    private static final String GET_SECRET_SUBCOMPONENTS_QUERY_PREFIX_SQL =
            "SELECT id, secret_id, component_type, value, encrypted, encryption_algorithm " +
                    "FROM secret_components " +
                    "WHERE component_type IN (:componentTypes) ";
    private static final String GET_SECRET_COMPONENTS_SUBQUERY_ID_CONDITION =
            "AND secret_id = :id ";
    private static final String GET_SECRET_COMPONENTS_SUBQUERY_OWNER_CONDITION =
            "AND secret_id IN (SELECT id FROM secrets WHERE owner = :owner) ";


    private static final String SAVE_SECRET_HEADER_SQL =
            "INSERT INTO secrets (id, image_name, owner, secret_type, name, comments, key_id) " +
                    "VALUES (:id, :imageName, :owner, :secretType, :name, :comments, :keyId) " +
                "ON CONFLICT (id) DO UPDATE " +
                    "SET image_name = :imageName, owner = :owner, secret_type = :secretType, name = :name, comments = :comments, key_id = :keyId";
    private static final String SAVE_SECRET_COMPONENT_SQL =
            "INSERT INTO secret_components (id, secret_id, component_type, value, encrypted, encryption_algorithm) " +
                    "VALUES (:id, :secretId, :componentType, :value, :encrypted, :encryptionAlgorithm) " +
                "ON CONFLICT (id) DO UPDATE " +
                    "SET secret_id = :secretId, component_type = :componentType, value = :value, encrypted = :encrypted, encryption_algorithm = :encryptionAlgorithm";
    private static final String COUNT_SECRETS_USING_KEY =
            "SELECT count(id) FROM secrets WHERE key_id = :keyId";
    private static final String DELETE_SECRET_SQL =
            "DELETE FROM secret_components WHERE secret_id = :id; " +
            "DELETE FROM secrets WHERE id = :id; ";


    private final NamedParameterJdbcTemplate template;

    @Autowired
    public SecretDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.template = namedParameterJdbcTemplate;
    }

    public Secret getSecretWithComponents(String secretId, String owner, Collection<String> componentTypes) {
        log.info("getSecretWithComponents: " + componentTypes);
        MapSqlParameterSource secretSource = new MapSqlParameterSource();
        secretSource.addValue("id", secretId);
        secretSource.addValue("owner", owner);
        secretSource.addValue("componentTypes", componentTypes);

        List<Secret> secrets = template.query(getSecretQuery(secretId, componentTypes), secretSource, rs -> { return parseSecretsWithComponents(rs, owner); });

        if (secrets.size() > 0) {
            return secrets.get(0);
        }

        return null;
    }

    public List<Secret> getSecretsWithComponents(String owner, Collection<String> componentTypes) {
        MapSqlParameterSource secretSource = new MapSqlParameterSource();
        secretSource.addValue("owner", owner);
        secretSource.addValue("componentTypes", componentTypes);

        return template.query(getSecretQuery("", componentTypes), secretSource, rs -> { return parseSecretsWithComponents(rs, owner); });
    }

    public int deleteSecret(String id) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", id);

        return template.update(DELETE_SECRET_SQL, source);
    }

    public int saveSecret(Secret secret) {
        MapSqlParameterSource headerSource = new MapSqlParameterSource();
        headerSource.addValue("id", secret.id());
        headerSource.addValue("imageName", secret.imageName());
        headerSource.addValue("owner", secret.owner());
        headerSource.addValue("secretType", secret.secretType());
        headerSource.addValue("name", secret.name());
        headerSource.addValue("comments", secret.comments());
        headerSource.addValue("keyId", secret.keyId());

        int rowsUpdated = template.update(SAVE_SECRET_HEADER_SQL, headerSource);

        if (rowsUpdated > 0 && secret.secretComponents() != null && secret.secretComponents().size() > 0) {
            MapSqlParameterSource[] componentsSource = new MapSqlParameterSource[secret.secretComponents().size()];
            for (int i = 0; i < secret.secretComponents().size(); i++) {
                componentsSource[i] = new MapSqlParameterSource();
                SecretComponent component =secret.secretComponents().get(i);

                componentsSource[i].addValue("id", component.id());
                componentsSource[i].addValue("secretId", component.secretId());
                componentsSource[i].addValue("componentType", component.componentType());
                componentsSource[i].addValue("value", component.value());
                componentsSource[i].addValue("encrypted", component.encrypted());
                componentsSource[i].addValue("encryptionAlgorithm", component.encryptionAlgorithm());
            }

            template.batchUpdate(SAVE_SECRET_COMPONENT_SQL, componentsSource);
        }

        return rowsUpdated;
    }

    public boolean isKeyInUse(String keyId) {
        return template.queryForObject(COUNT_SECRETS_USING_KEY, Map.of("keyId", keyId), Integer.class) > 0;
    }

    private List<Secret> parseSecretsWithComponents(ResultSet rs, String owner) throws SQLException {
        List<Secret> secrets = new ArrayList<>();

        String lastId = "";
        String imageName = "";
        String type = "";
        String name = "";
        String comments = "";
        String keyId = "";
        List<SecretComponent> secretComponents = new ArrayList<>();

        while (rs.next()) {
            String secretId = rs.getString("id");

            if (!lastId.equals(secretId)) {
                if (lastId != "") {
                    secrets.add(new Secret(lastId, "", imageName, type, name, comments, keyId, secretComponents));
                }

                lastId = secretId;
                imageName = rs.getString("image_name");
                type = rs.getString("secret_type");
                name = rs.getString("name");
                comments = rs.getString("comments");
                keyId = rs.getString("key_id");
                secretComponents = new ArrayList<>();
            }

            String componentId = rs.getString("c_id");
            if (componentId != null && !componentId.isBlank()) {
                secretComponents.add(new SecretComponent(
                        componentId,
                        secretId,
                        rs.getString("component_type"),
                        rs.getString("value"),
                        rs.getBoolean("encrypted"),
                        rs.getString("encryption_algorithm")));
            }
        }

        if (lastId != "") {
            secrets.add(new Secret(lastId, owner, imageName, type, name, comments, keyId, secretComponents));
        }

        return secrets;
    }

    private static String getSecretQuery(String id, Collection<String> componentTypes) {
        boolean hasComponents = componentTypes != null && componentTypes.size() > 0;
        boolean hasId = id != null && !id.isEmpty();

        if (hasComponents) {
            return GET_SECRET_WITH_COMPONENTS_QUERY_PREFIX_SQL +
                    GET_SECRET_SUBCOMPONENTS_QUERY_PREFIX_SQL +
                    (hasId ? GET_SECRET_COMPONENTS_SUBQUERY_ID_CONDITION : GET_SECRET_COMPONENTS_SUBQUERY_OWNER_CONDITION) +
                    GET_SECRET_WITH_COMPONENTS_QUERY_SUFFIX_SQL +
                    (hasId ? GET_SECRET_QUERY_COMPONENT_ID_MATCH_SQL : "") +
                    GET_SECRET_QUERY_COMPONENT_ID_ORDERING_SQL;
        }

        return GET_SECRET_NO_COMPONENTS_SQL +
                (hasId ? GET_SECRET_QUERY_COMPONENT_ID_MATCH_SQL : "") +
                GET_SECRET_QUERY_COMPONENT_ID_ORDERING_SQL;
    }
}
