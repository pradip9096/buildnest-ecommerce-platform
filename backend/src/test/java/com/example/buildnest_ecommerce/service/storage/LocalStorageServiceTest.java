package com.example.buildnest_ecommerce.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalStorageService")
class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalStorageService(tempDir.toString());
    }

    // ── Rejection paths ────────────────────────────────────────────────────────

    @Test
    @DisplayName("store throws when file is null")
    void store_nullFile_throws() {
        StorageException ex = assertThrows(StorageException.class,
                () -> service.store(null));
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("store throws when file is empty")
    void store_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[0]);

        StorageException ex = assertThrows(StorageException.class,
                () -> service.store(file));
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("store throws when file exceeds 10 MB")
    void store_fileTooLarge_throws() {
        byte[] oversized = new byte[(int) (10L * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", oversized);

        StorageException ex = assertThrows(StorageException.class,
                () -> service.store(file));
        assertTrue(ex.getMessage().contains("maximum allowed size"));
    }

    @Test
    @DisplayName("store throws when content type is null")
    void store_nullContentType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", null, new byte[]{1, 2, 3});

        StorageException ex = assertThrows(StorageException.class,
                () -> service.store(file));
        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }

    @Test
    @DisplayName("store throws when content type is not an allowed image type")
    void store_unsupportedContentType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        StorageException ex = assertThrows(StorageException.class,
                () -> service.store(file));
        assertTrue(ex.getMessage().contains("Unsupported file type"));
        assertTrue(ex.getMessage().contains("application/pdf"));
    }

    // ── Happy paths ────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "image/jpeg, .jpg",
            "image/png,  .png",
            "image/webp, .webp",
            "image/gif,  .gif"
    })
    @DisplayName("store accepts all allowed image types and returns correct extension")
    void store_allowedContentType_returnsUrlWithCorrectExtension(
            String contentType, String expectedExtension) {

        MockMultipartFile file = new MockMultipartFile(
                "file", "image" + expectedExtension, contentType, new byte[]{1, 2, 3});

        String url = service.store(file);

        assertTrue(url.startsWith("/uploads/"),
                "URL must start with /uploads/ but was: " + url);
        assertTrue(url.endsWith(expectedExtension.trim()),
                "URL must end with " + expectedExtension + " but was: " + url);
    }

    @Test
    @DisplayName("store writes the file to disk")
    void store_validFile_fileExistsOnDisk() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", new byte[]{10, 20, 30});

        String url = service.store(file);

        String filename = url.substring("/uploads/".length());
        Path stored = tempDir.resolve(filename);
        assertTrue(Files.exists(stored), "Stored file must exist at: " + stored);
        assertArrayEquals(new byte[]{10, 20, 30}, Files.readAllBytes(stored));
    }

    @Test
    @DisplayName("store returns a different URL on each call (UUID uniqueness)")
    void store_calledTwice_returnsDifferentUrls() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String url1 = service.store(file);
        String url2 = service.store(file);

        assertNotEquals(url1, url2, "Each store() call must produce a unique filename");
    }

    // ── I/O failure path ───────────────────────────────────────────────────────

    @Test
    @DisplayName("store wraps IOException in StorageException")
    void store_inputStreamThrows_wrapsInStorageException() {
        MultipartFile broken = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1}) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("disk full");
            }
        };

        StorageException ex = assertThrows(StorageException.class,
                () -> service.store(broken));
        assertNotNull(ex.getCause(), "StorageException must wrap the original IOException");
        assertInstanceOf(IOException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("Failed to store file"));
    }
}
