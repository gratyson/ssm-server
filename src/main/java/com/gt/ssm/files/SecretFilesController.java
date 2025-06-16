package com.gt.ssm.files;

import com.google.crypto.tink.subtle.Base64;
import com.gt.ssm.exception.DaoException;
import com.gt.ssm.exception.KeyException;
import com.gt.ssm.exception.SecretEncryptionException;
import com.gt.ssm.exception.SecretFileTooLargeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/rest/files")
public class SecretFilesController {

    private static final Logger log = LoggerFactory.getLogger(SecretFilesController.class);

    private final SecretFilesService secretFilesService;

    @Autowired
    public SecretFilesController(SecretFilesService secretFilesService) {
        this.secretFilesService = secretFilesService;
    }

    @PutMapping("save")
    public SaveFileResult saveSecretFile(@AuthenticationPrincipal UserDetails userDetails, @RequestPart("request") SaveFileRequest saveFileRequest, @RequestPart("secretFile") MultipartFile secretFile) throws IOException {
        try {
            String fileId = secretFilesService.saveSecretFile(userDetails.getUsername(), saveFileRequest.name, saveFileRequest.keyId, saveFileRequest.keyPassword, secretFile.getBytes());

            return new SaveFileResult(true, fileId, "");
        } catch (SecretFileTooLargeException ex) {
            return new SaveFileResult(false, "", "Requested file size exceeded max allowed size");
        } catch (SecretEncryptionException ex) {
            return new SaveFileResult(false, "", "Failed to encrypted file data");
        } catch (DaoException ex) {
            return new SaveFileResult(false, "", "An error occurred saving the file to the database");
        } catch (Exception ex) {
            log.error("Failed to save image", ex);
            return new SaveFileResult(false, "", "An unknown error occurred");
        }
    }

    @PostMapping("load")
    public ResponseEntity<byte[]> loadSecretFile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody() LoadFileRequest loadFileRequest) {
        try {
            byte[] bytes = secretFilesService.loadSecretFile(userDetails.getUsername(), loadFileRequest.fileId, loadFileRequest.keyId, loadFileRequest.keyPassword);

            return ResponseEntity.ok().header("Content-Disposition", "attachment").body(bytes);
        } catch (KeyException | SecretEncryptionException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new byte[0]);
        } catch (Exception ex) {
            log.error("Failed to load image", ex);
            return ResponseEntity.internalServerError().body(new byte[0]);
        }
    }

    private record SaveFileRequest(String name, String keyId, String keyPassword) { }
    private record SaveFileResult(boolean success, String fileId, String errorMsg) { }
    private record LoadFileRequest(String fileId, String fileName, String keyId, String keyPassword) { }
}
