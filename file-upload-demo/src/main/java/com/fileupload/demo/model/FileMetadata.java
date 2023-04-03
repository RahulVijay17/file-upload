package com.fileupload.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "new_file_name")
    private String newFileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    public FileMetadata(String fileName, String newFileName, String fileType, Long fileSize) {
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadTime = LocalDateTime.now();
    }
}