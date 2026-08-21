package com.fileshare.file.controller;

import com.fileshare.auth.security.UserPrincipal;
import com.fileshare.common.exception.BadRequestException;
import com.fileshare.common.exception.GlobalExceptionHandler;
import com.fileshare.file.dto.FileResponseDto;
import com.fileshare.file.service.FileService;
import com.fileshare.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class FileUploadControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private MockMvc mockMvc;
    private UserPrincipal testUserPrincipal;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUserPrincipal = new UserPrincipal(userId, "uploader@example.com", "password", Role.USER, Collections.emptyList());

        HandlerMethodArgumentResolver userPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUserPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(userPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("Valid file upload returns 201 Created with expected response body and fields")
    void uploadFile_ValidFile_ReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "Sample PDF content bytes".getBytes()
        );

        UUID generatedFileId = UUID.randomUUID();
        FileResponseDto responseDto = FileResponseDto.builder()
                .id(generatedFileId)
                .originalFilename("document.pdf")
                .contentType("application/pdf")
                .sizeBytes(24L)
                .sha256Hash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .currentVersionNumber(1)
                .deduplicated(false)
                .ownerId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        when(fileService.uploadFile(any(), eq(null), eq(userId))).thenReturn(responseDto);

        MvcResult result = mockMvc.perform(multipart("/api/v1/files/upload").file(file))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody).contains("document.pdf");
        assertThat(responseBody).contains(generatedFileId.toString());
        assertThat(responseBody).contains("File uploaded successfully");
        assertThat(responseBody).contains("\"success\":true");
        assertThat(responseBody).contains("\"status\":201");
    }

    @Test
    @DisplayName("Invalid file type or extension returns 400 Bad Request")
    void uploadFile_InvalidFileType_ReturnsBadRequest() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/x-msdownload",
                "binary content".getBytes()
        );

        when(fileService.uploadFile(any(), eq(null), eq(userId)))
                .thenThrow(new BadRequestException("Invalid file extension: .exe is not allowed"));

        MvcResult result = mockMvc.perform(multipart("/api/v1/files/upload").file(invalidFile))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody).contains("Invalid file extension: .exe is not allowed");
        assertThat(responseBody).contains("\"success\":false");
        assertThat(responseBody).contains("\"status\":400");
    }
}
