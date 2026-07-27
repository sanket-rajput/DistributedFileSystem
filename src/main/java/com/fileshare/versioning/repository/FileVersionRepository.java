package com.fileshare.versioning.repository;

import com.fileshare.versioning.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {

    List<FileVersion> findByFileMetadataIdOrderByVersionNumberDesc(UUID fileMetadataId);

    Optional<FileVersion> findByFileMetadataIdAndVersionNumber(UUID fileMetadataId, int versionNumber);
}
