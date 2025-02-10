package com.gt.ssm.image;

import com.gt.ssm.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.List;

@Component
public class ImageDao {

    private static final Logger log = LoggerFactory.getLogger(ImageDao.class);

    private static final String SAVE_IMAGE_SQL =
            "BEGIN TRANSACTION; " +
                "INSERT INTO images (image_name, image_oid) VALUES (?, ?) " +
                "ON CONFLICT (image_name) DO UPDATE " +
                "     SET image_oid = excluded.image_oid; " +
            "COMMIT TRANSACTION;";

    private static final String LOAD_IMAGE_SQL =
            "SELECT image_oid FROM images WHERE image_name = ?";

    private static final String DELETE_IMAGE_SQL =
            "BEGIN TRANSACTION; " +
                "DELETE FROM images where image_name = ?; " +
            "COMMIT TRANSACTION;";

    private final Connection databaseConnection;

    @Autowired
    public ImageDao(Connection blobDatabaseConnection) {
        this.databaseConnection = blobDatabaseConnection;
    }

    public void saveImageFile(String imageName, ByteBuffer bytes) {
        try {
            databaseConnection.beginRequest();
            PreparedStatement ps = databaseConnection.prepareStatement(SAVE_IMAGE_SQL);

            ps.setString(1, imageName);
            ps.setBlob(2, new ByteArrayInputStream(bytes.array()), bytes.array().length);

            ps.executeUpdate();
            databaseConnection.commit();
            databaseConnection.endRequest();

        } catch (SQLException ex) {
            String errMsg = "Error attempting to save file " + imageName + ". ";
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

    public ByteBuffer loadImageFile(String imageName) {
        try {
            ByteBuffer bytes;

            PreparedStatement ps = databaseConnection.prepareStatement(LOAD_IMAGE_SQL);
            ps.setString(1, imageName);

            ResultSet rs = ps.executeQuery();
            if (rs != null && rs.next()) {
                Blob blob = rs.getBlob(1);
                bytes = ByteBuffer.wrap(blob.getBinaryStream().readAllBytes());
            } else {
                log.warn("No binary data available for image file " + imageName);
                bytes = ByteBuffer.allocate(0);
            }

            return bytes;
        } catch (SQLException | IOException ex) {
            String errMsg = "Error attempting to load file " + imageName + " from blob server";
            log.error(errMsg, ex);
            throw new DaoException(errMsg, ex);
        }
    }

    public int deleteImage(String imageName) {
        try {
            databaseConnection.beginRequest();
            PreparedStatement ps = databaseConnection.prepareStatement(DELETE_IMAGE_SQL);
            ps.setString(1, imageName);

            int updateCnt = ps.executeUpdate();
            databaseConnection.commit();
            databaseConnection.endRequest();

            return updateCnt;
        } catch (SQLException ex) {
            String errMsg = "Error attempting to image file from blob server. ";
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
}
