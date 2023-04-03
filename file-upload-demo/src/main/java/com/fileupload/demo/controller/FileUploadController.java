package com.fileupload.demo.controller;

import com.fileupload.demo.exception.FileStorageException;
import com.fileupload.demo.exception.InvalidFileException;
import com.fileupload.demo.impl.FileStorageServiceImpl;
import com.fileupload.demo.model.FileMetadata;
import com.fileupload.demo.repository.FileMetadataRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private FileStorageServiceImpl fileUploadService;
    @Autowired
    private FileMetadataRepository fileMetadataRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // Save the file to disk and metadata to database
            fileUploadService.storeFile(file);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (FileStorageException | InvalidFileException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file.");
        }
    }
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws FileNotFoundException {
        // Get the file metadata from the database using the fileId
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        // Create the path to the file
        Path filePath = Paths.get(uploadDir, fileMetadata.getNewFileName());

        // Create a Resource object from the file
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Failed to download file");
        }

        // Return the file as a response entity with content-disposition header
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}