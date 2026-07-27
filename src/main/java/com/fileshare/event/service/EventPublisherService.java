package com.fileshare.event.service;

import com.fileshare.event.dto.FileDeletedEvent;
import com.fileshare.event.dto.FileSharedEvent;
import com.fileshare.event.dto.FileUploadedEvent;

public interface EventPublisherService {

    void publishFileUploaded(FileUploadedEvent event);

    void publishFileShared(FileSharedEvent event);

    void publishFileDeleted(FileDeletedEvent event);
}
