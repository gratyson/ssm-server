package com.gt.ssm.image;

import com.gt.ssm.exception.BlobException;
import com.gt.ssm.exception.ImageException;
import com.gt.ssm.exception.ImageToLargeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageDao imageDao;
    private final int maxImageSize;

    @Autowired
    public ImageService(ImageDao imageDao, @Value("${ssm.image.maxSize}") int maxImageSize) {
        this.imageDao = imageDao;
        this.maxImageSize = maxImageSize;
    }

    public void saveImage(String imageName, byte[] imageBytes) {
        if (imageBytes.length > maxImageSize) {
            throw new ImageToLargeException(String.format("Image size {} exceeds max allowed size of {}.", imageBytes.length, maxImageSize));
        }

        imageDao.saveImageFile(imageName, ByteBuffer.wrap(imageBytes));
    }

    public byte[] loadImage(String imageName) {
        ByteBuffer imageByteBuffer = imageDao.loadImageFile(imageName);

        if (imageByteBuffer != null) {
            return imageByteBuffer.array();
        }

        log.warn("Unable to find image " + imageName);
        return new byte[0];
    }

    public boolean deleteImage(String imageName) {
        return imageDao.deleteImage(imageName) > 0;
    }
}
