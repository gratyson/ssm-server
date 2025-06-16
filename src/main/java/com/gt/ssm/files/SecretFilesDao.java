package com.gt.ssm.files;

import com.gt.ssm.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;

@Component
public class SecretFilesDao {

    private static final Logger log = LoggerFactory.getLogger(SecretFilesDao.class);

    private static final String SAVE_FILE_SQL =
            "BEGIN TRANSACTION; " +
                "INSERT INTO secret_files (file_id, owner, md5_hash, encryption_algorithm, file_oid) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (file_id) DO UPDATE " +
                "     SET file_oid = excluded.file_oid; " +
            "COMMIT TRANSACTION;";

    private static final String LOAD_FILE_SQL =
            "SELECT md5_hash, encryption_algorithm, file_oid FROM secret_files WHERE file_id = ?";

    private static final String DELETE_FILE_SQL =
            "BEGIN TRANSACTION; " +
                "DELETE FROM secret_files WHERE owner = ? AND file_id = ?; " +
            "COMMIT TRANSACTION;";

    private final Connection databaseConnection;

    @Autowired
    public SecretFilesDao(Connection blobDatabaseConnection) {
        this.databaseConnection = blobDatabaseConnection;
    }

    public void saveSecretFile(String secretFileId, String secretFileName, String owner, byte[] encrpytedFileBytes, String md5Hash, String encryptionAlgorithm) {
        try {
            databaseConnection.beginRequest();
            PreparedStatement ps = databaseConnection.prepareStatement(SAVE_FILE_SQL);

            ps.setString(1, secretFileId);
            ps.setString(2, owner);
            ps.setString(3, md5Hash);
            ps.setString(4, encryptionAlgorithm);
            ps.setBlob(5, new ByteArrayInputStream(encrpytedFileBytes), encrpytedFileBytes.length);

            ps.executeUpdate();
            databaseConnection.commit();
            databaseConnection.endRequest();

        } catch (SQLException ex) {
            String errMsg = "Error attempting to save file " + secretFileName + ". ";
            try {
                databaseConnection.rollback();
                databaseConnection.endRequest();
            } catch (SQLException ex2) {
                errMsg += "Failed to rollback transaction, connection in likely in an unstable state.";
            }

            log.error(errMsg, ex);
            throw new DaoException(errMsg, ex);
        }
    }

    public EncryptedFileData loadSecretFile(String fileId) {
        try {
            byte[] bytes = new byte[0];
            String md5Hash = "";
            String encryptionAlgorithm = "";

            PreparedStatement ps = databaseConnection.prepareStatement(LOAD_FILE_SQL);
            ps.setString(1, fileId);

            ResultSet rs = ps.executeQuery();
            if (rs != null && rs.next()) {
                md5Hash = rs.getString(1);
                encryptionAlgorithm = rs.getString(2);

                Blob blob = rs.getBlob(3);
                bytes = blob.getBinaryStream().readAllBytes();
            } else {
                log.warn("No binary data available for secret file " + fileId);
            }

            return new EncryptedFileData(bytes, md5Hash, encryptionAlgorithm);
        } catch (SQLException | IOException ex) {
            String errMsg = "Error attempting to load file " + fileId + ".";
            log.error(errMsg, ex);
            throw new DaoException(errMsg, ex);
        }
    }

    public int deleteSecretFile(String owner, String fileId) {
        try {
            databaseConnection.beginRequest();
            PreparedStatement ps = databaseConnection.prepareStatement(DELETE_FILE_SQL);
            ps.setString(1, owner);
            ps.setString(2, fileId);

            int updateCnt = ps.executeUpdate();
            databaseConnection.commit();
            databaseConnection.endRequest();

            return updateCnt;
        } catch (SQLException ex) {
            String errMsg = "Error attempting to file. ";
            try {
                databaseConnection.rollback();
                databaseConnection.endRequest();
            } catch (SQLException ex2) {
                errMsg += "Failed to rollback transaction, connection in likely in an unstable state.";
            }

            log.error(errMsg, ex);
            throw new DaoException(errMsg, ex);
        }
    }

    protected record EncryptedFileData(byte[] bytes, String md5Hash, String encryptionAlogrithm) { }
}
