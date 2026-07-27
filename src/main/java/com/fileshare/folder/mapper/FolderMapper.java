package com.fileshare.folder.mapper;

import com.fileshare.folder.dto.FolderResponseDto;
import com.fileshare.folder.entity.Folder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FolderMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "parentFolderId", source = "parentFolder.id")
    FolderResponseDto toDto(Folder folder);
}
