package com.fileupload.demo.impl;

import com.fileupload.demo.exception.FileStorageException;
import com.fileupload.demo.exception.InvalidFileException;
import com.fileupload.demo.model.FileMetadata;
import com.fileupload.demo.repository.FileMetadataRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileStorageServiceImpl {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;


    public void storeFile(MultipartFile file) throws FileStorageException, InvalidFileException, IOException {
        // Get the filename and extension of the uploaded file
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(fileName);

        // Check if the file is empty or has invalid extension
        if (file.isEmpty() || !Arrays.asList("jpg", "jpeg", "png").contains(fileExtension.toLowerCase())) {
            throw new InvalidFileException("Invalid file format. Please upload a JPG, JPEG, or PNG file.");
        }

        // Save the file to disk
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension.toLowerCase();
        Path filePath = Paths.get(uploadDir, newFileName);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to upload file.", e);
        }

        // Save file metadata to database
        FileMetadata fileMetadata = new FileMetadata(fileName, newFileName, file.getContentType(), file.getSize());
        fileMetadataRepository.save(fileMetadata);
    }

    public Resource downloadFile(Long fileId) throws FileNotFoundException {
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

        return resource;
    }
}