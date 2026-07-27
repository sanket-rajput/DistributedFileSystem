package com.fileshare.sharing.mapper;

import com.fileshare.file.mapper.FileMapper;
import com.fileshare.sharing.dto.ShareResponseDto;
import com.fileshare.sharing.entity.Share;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {FileMapper.class})
public interface ShareMapper {

    @Mapping(target = "file", source = "fileMetadata")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "shareUrl", ignore = true)
    ShareResponseDto toDto(Share share);
}
