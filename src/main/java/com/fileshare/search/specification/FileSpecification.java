package com.fileshare.search.specification;

import com.fileshare.file.entity.FileMetadata;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileSpecification {

    public static Specification<FileMetadata> getSearchSpecification(
            String name,
            String contentType,
            UUID folderId,
            Long minSize,
            Long maxSize,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            UUID ownerId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory ownership restriction
            if (ownerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner").get("id"), ownerId));
            }

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("originalFilename")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (contentType != null && !contentType.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("contentType")),
                        contentType.toLowerCase()
                ));
            }

            if (folderId != null) {
                predicates.add(criteriaBuilder.equal(root.get("folder").get("id"), folderId));
            }

            if (minSize != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sizeBytes"), minSize));
            }

            if (maxSize != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sizeBytes"), maxSize));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
