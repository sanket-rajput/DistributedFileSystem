package com.fileshare.versioning.mapper;

import com.fileshare.versioning.dto.FileVersionResponseDto;
import com.fileshare.versioning.entity.FileVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileVersionMapper {

    @Mapping(target = "fileId", source = "fileMetadata.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    FileVersionResponseDto toDto(FileVersion fileVersion);
}
