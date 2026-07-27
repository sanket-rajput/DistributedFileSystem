package com.fileshare.integration;

import com.fileshare.folder.dto.CreateFolderRequest;
import com.fileshare.auth.dto.LoginRequest;
import com.fileshare.auth.dto.RegisterRequest;
import com.fileshare.sharing.dto.CreateShareRequest;
import com.fileshare.sharing.entity.SharePermission;
import com.fileshare.user.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FileSharingE2EIntegrationIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullEndToEndLifecycleTest() {
        // 1. User Registration
        RegisterRequest regReq = new RegisterRequest("e2e_user@example.com", "password123", Role.USER);
        ResponseEntity<Map> regResp = restTemplate.postForEntity("/api/v1/auth/register", regReq, Map.class);
        assertEquals(HttpStatus.CREATED, regResp.getStatusCode());

        // 2. User Login
        LoginRequest loginReq = new LoginRequest("e2e_user@example.com", "password123");
        ResponseEntity<Map> loginResp = restTemplate.postForEntity("/api/v1/auth/login", loginReq, Map.class);
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        Map data = (Map) loginResp.getBody().get("data");
        String token = (String) data.get("accessToken");
        assertNotNull(token);

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);

        // 3. Create Folder
        CreateFolderRequest folderReq = new CreateFolderRequest("E2E_Folder", null);
        HttpEntity<CreateFolderRequest> folderEntity = new HttpEntity<>(folderReq, authHeaders);
        ResponseEntity<Map> folderResp = restTemplate.exchange("/api/v1/folders", HttpMethod.POST, folderEntity, Map.class);
        assertEquals(HttpStatus.CREATED, folderResp.getStatusCode());
        Map folderData = (Map) folderResp.getBody().get("data");
        String folderId = (String) folderData.get("id");

        // 4. Upload File
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("Integration Test Content".getBytes()) {
            @Override
            public String getFilename() {
                return "e2e_test.txt";
            }
        });
        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setBearerAuth(token);
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<Map> uploadResp = restTemplate.exchange("/api/v1/files/upload?folderId=" + folderId, HttpMethod.POST, uploadEntity, Map.class);
        assertEquals(HttpStatus.CREATED, uploadResp.getStatusCode());
        Map fileData = (Map) uploadResp.getBody().get("data");
        String fileId = (String) fileData.get("id");
        assertFalse((Boolean) fileData.get("deduplicated"));

        // 5. Upload Duplicate File (Verify SHA-256 Deduplication)
        ResponseEntity<Map> dedupResp = restTemplate.exchange("/api/v1/files/upload?folderId=" + folderId, HttpMethod.POST, uploadEntity, Map.class);
        assertEquals(HttpStatus.CREATED, dedupResp.getStatusCode());
        Map dedupData = (Map) dedupResp.getBody().get("data");
        assertTrue((Boolean) dedupData.get("deduplicated"));

        // 6. Create Share Link
        CreateShareRequest shareReq = new CreateShareRequest(null, SharePermission.DOWNLOAD);
        HttpEntity<CreateShareRequest> shareEntity = new HttpEntity<>(shareReq, authHeaders);
        ResponseEntity<Map> shareResp = restTemplate.exchange("/api/v1/files/" + fileId + "/share", HttpMethod.POST, shareEntity, Map.class);
        assertEquals(HttpStatus.CREATED, shareResp.getStatusCode());
        Map shareData = (Map) shareResp.getBody().get("data");
        String shareToken = (String) shareData.get("token");
        String shareId = (String) shareData.get("id");

        // 7. Access Public Share Link (Unauthenticated)
        ResponseEntity<byte[]> publicResp = restTemplate.getForEntity("/api/v1/share/" + shareToken, byte[].class);
        assertEquals(HttpStatus.OK, publicResp.getStatusCode());
        assertNotNull(publicResp.getBody());

        // 8. Revoke Share Link
        HttpEntity<Void> revokeEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<Map> revokeResp = restTemplate.exchange("/api/v1/files/" + fileId + "/share/" + shareId, HttpMethod.DELETE, revokeEntity, Map.class);
        assertEquals(HttpStatus.OK, revokeResp.getStatusCode());

        // 9. Access Revoked Share Link -> Verify Denied (403)
        ResponseEntity<Map> deniedPublicResp = restTemplate.getForEntity("/api/v1/share/" + shareToken, Map.class);
        assertEquals(HttpStatus.FORBIDDEN, deniedPublicResp.getStatusCode());
    }
}
