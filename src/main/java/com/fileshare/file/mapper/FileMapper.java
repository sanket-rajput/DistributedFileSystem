package com.fileshare.file.mapper;

import com.fileshare.file.dto.FileResponseDto;
import com.fileshare.file.entity.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "folderId", source = "folder.id")
    FileResponseDto toDto(FileMetadata fileMetadata);
}
