package com.gt.ssm.image;

import com.gt.ssm.exception.DaoException;
import com.gt.ssm.exception.ImageToLargeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;

@RestController
@RequestMapping("/rest/image")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PutMapping()
    public SaveImageResult saveImageFile(@RequestPart("name") SaveImageInput saveImageInput, @RequestPart("imageFile") MultipartFile imageFile) throws IOException {
        try {
            imageService.saveImage(saveImageInput.name, imageFile.getBytes());
            return new SaveImageResult(true, "");
        } catch (ImageToLargeException ex) {
            return new SaveImageResult(false, "Requested image size exceeded max allowed size");
        } catch (DaoException ex) {
            return new SaveImageResult(false, "An error occurred saving the image to the database");
        } catch (Exception ex) {
            log.error("Failed to save image", ex);
            return new SaveImageResult(false, "An unknown error occurred");
        }
    }

    @GetMapping(produces = "image/*")
    public byte[] loadImageFile(@RequestParam("name") String name) {
        return imageService.loadImage(name);
    }

    @DeleteMapping()
    public DeleteImageResult deleteImage(@RequestBody String name) {
        try {
            imageService.deleteImage(name);
            return new DeleteImageResult(true, "");
        } catch (DaoException ex) {
            return new DeleteImageResult(false, "An error occurred deleting the image from the database");
        } catch (Exception ex) {
            return new DeleteImageResult(false, "An unknown error occurred");
        }
    }

    private record SaveImageInput(String name) { }
    private record SaveImageResult(boolean success, String errorMsg) { }
    private record DeleteImageResult(boolean success, String errorMsg) { }
}
