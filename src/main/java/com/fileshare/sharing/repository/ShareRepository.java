package com.fileshare.sharing.repository;

import com.fileshare.sharing.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShareRepository extends JpaRepository<Share, UUID> {

    Optional<Share> findByToken(String token);

    Optional<Share> findByIdAndFileMetadataOwnerId(UUID id, UUID ownerId);

    Optional<Share> findFirstByFileMetadataIdAndRevokedFalseOrderByCreatedAtDesc(UUID fileId);

    List<Share> findByFileMetadataIdAndRevokedFalse(UUID fileId);
}
