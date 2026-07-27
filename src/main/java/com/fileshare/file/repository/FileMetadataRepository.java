package com.fileshare.file.repository;

import com.fileshare.file.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID>, JpaSpecificationExecutor<FileMetadata> {

    List<FileMetadata> findByOwnerIdAndFolderId(UUID ownerId, UUID folderId);

    List<FileMetadata> findByOwnerIdAndFolderIsNull(UUID ownerId);

    Optional<FileMetadata> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<FileMetadata> findByFolderId(UUID folderId);

    Optional<FileMetadata> findFirstByOwnerIdAndSha256Hash(UUID ownerId, String sha256Hash);

    Optional<FileMetadata> findFirstBySha256Hash(String sha256Hash);

    Optional<FileMetadata> findByOwnerIdAndFolderIdAndOriginalFilename(UUID ownerId, UUID folderId, String originalFilename);

    Optional<FileMetadata> findByOwnerIdAndFolderIsNullAndOriginalFilename(UUID ownerId, String originalFilename);
}
