package com.fileshare.folder.entity;

import com.fileshare.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Folder() {
    }

    public Folder(UUID id, String name, User owner, Folder parentFolder, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.parentFolder = parentFolder;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Folder getParentFolder() { return parentFolder; }
    public void setParentFolder(Folder parentFolder) { this.parentFolder = parentFolder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static FolderBuilder builder() {
        return new FolderBuilder();
    }

    public static class FolderBuilder {
        private UUID id;
        private String name;
        private User owner;
        private Folder parentFolder;
        private LocalDateTime createdAt;

        public FolderBuilder id(UUID id) { this.id = id; return this; }
        public FolderBuilder name(String name) { this.name = name; return this; }
        public FolderBuilder owner(User owner) { this.owner = owner; return this; }
        public FolderBuilder parentFolder(Folder parentFolder) { this.parentFolder = parentFolder; return this; }
        public FolderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Folder build() {
            return new Folder(id, name, owner, parentFolder, createdAt);
        }
    }
}
