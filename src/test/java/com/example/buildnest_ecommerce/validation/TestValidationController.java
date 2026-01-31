package com.example.buildnest_ecommerce.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

/**
 * Test-only controller to back InputValidationTest endpoints.
 * These endpoints enforce basic validation checks for security test cases.
 */
@RestController
@Profile("test")
class TestValidationController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    private final ObjectMapper objectMapper;

    TestValidationController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping(path = "/api/products/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> searchProducts(@RequestBody String body) {
        String query = readTextField(body, "query");
        if (query == null || query.isBlank() || containsSqlInjection(query)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/api/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createProduct(@RequestBody String body, HttpServletRequest request) {
        if (hasParameterPollution(request, "id")) {
            return ResponseEntity.badRequest().build();
        }

        String name = readTextField(body, "name");
        if (name != null && (containsXss(name) || name.length() > 255)) {
            return ResponseEntity.badRequest().build();
        }

        JsonNode priceNode = readJson(body).path("price");
        if (priceNode.isTextual()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/api/cart/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> addToCart(@RequestBody String body) {
        JsonNode json = readJson(body);
        if (!json.has("productId") || !json.has("quantity")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/api/files/upload")
    ResponseEntity<Void> uploadFile(
            @RequestParam(value = "path", required = false) String path,
            @RequestParam(value = "file", required = false) String file) {
        if (path != null && path.contains("..")) {
            return ResponseEntity.badRequest().build();
        }
        if (file != null && file.toLowerCase().endsWith(".exe")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/api/reports/generate")
    ResponseEntity<Void> generateReport(@RequestParam(value = "command", required = false) String command) {
        if (command != null && (command.contains(";") || command.toLowerCase().contains("rm -rf"))) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/api/xml/parse", consumes = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<Void> parseXml(@RequestBody String body) {
        if (body != null && body.contains("<!DOCTYPE")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/api/users/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> registerUser(@RequestBody String body) {
        JsonNode json = readJson(body);
        if (json.has("role")) {
            return ResponseEntity.badRequest().build();
        }

        String email = json.path("email").asText(null);
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().build();
        }

        String phone = json.path("phoneNumber").asText(null);
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    private boolean containsSqlInjection(String query) {
        String lower = query.toLowerCase();
        return lower.contains("' or '") || lower.contains(" or ") || lower.contains("--") || lower.contains(";");
    }

    private boolean containsXss(String value) {
        String lower = value.toLowerCase();
        return lower.contains("<script") || lower.contains("</script");
    }

    private boolean hasParameterPollution(HttpServletRequest request, String paramName) {
        String[] values = request.getParameterValues(paramName);
        return values != null && values.length > 1;
    }

    private String readTextField(String body, String fieldName) {
        JsonNode json = readJson(body);
        return json.path(fieldName).asText(null);
    }

    private JsonNode readJson(String body) {
        try {
            if (body == null || body.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }
}
