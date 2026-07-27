package com.fileshare.folder.repository;

import com.fileshare.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByOwnerIdAndParentFolderId(UUID ownerId, UUID parentFolderId);

    List<Folder> findByOwnerIdAndParentFolderIsNull(UUID ownerId);

    Optional<Folder> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByNameAndOwnerIdAndParentFolderId(String name, UUID ownerId, UUID parentFolderId);

    boolean existsByNameAndOwnerIdAndParentFolderIsNull(String name, UUID ownerId);
}
