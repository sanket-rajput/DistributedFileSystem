package com.fileshare.file.repository;

import com.fileshare.file.entity.FileMetadata;
import com.fileshare.user.entity.Role;
import com.fileshare.user.entity.User;
import com.fileshare.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
public class FileMetadataRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Save file metadata entity, retrieve it by id, assert all fields persisted correctly")
    void saveAndFindById_PersistsAllFieldsCorrectly() {
        // 1. Create and save owner user (required for non-null owner relationship)
        User owner = User.builder()
                .email("owner@example.com")
                .passwordHash("hashed_password_123")
                .role(Role.USER)
                .build();
        User savedOwner = userRepository.save(owner);
        assertThat(savedOwner.getId()).isNotNull();

        // 2. Create and save FileMetadata entity
        FileMetadata metadata = FileMetadata.builder()
                .originalFilename("report.pdf")
                .storageKey("storage-key-uuid-123")
                .sizeBytes(2048L)
                .contentType("application/pdf")
                .sha256Hash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .currentVersionNumber(1)
                .owner(savedOwner)
                .createdAt(LocalDateTime.now())
                .build();

        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);
        assertThat(savedMetadata.getId()).isNotNull();

        // 3. Retrieve by ID
        Optional<FileMetadata> retrievedOpt = fileMetadataRepository.findById(savedMetadata.getId());

        // 4. Assert all fields persisted correctly using AssertJ
        assertThat(retrievedOpt).isPresent();
        FileMetadata retrieved = retrievedOpt.get();

        assertThat(retrieved.getId()).isEqualTo(savedMetadata.getId());
        assertThat(retrieved.getOriginalFilename()).isEqualTo("report.pdf");
        assertThat(retrieved.getStorageKey()).isEqualTo("storage-key-uuid-123");
        assertThat(retrieved.getSizeBytes()).isEqualTo(2048L);
        assertThat(retrieved.getContentType()).isEqualTo("application/pdf");
        assertThat(retrieved.getSha256Hash()).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        assertThat(retrieved.getCurrentVersionNumber()).isEqualTo(1);
        assertThat(retrieved.getOwner().getId()).isEqualTo(savedOwner.getId());
        assertThat(retrieved.getOwner().getEmail()).isEqualTo("owner@example.com");
        assertThat(retrieved.getCreatedAt()).isNotNull();
    }
}
