package com.studproj.axiom.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullFlowIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public S3Client s3Client() {
            return mock(S3Client.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String accessToken;
    private static String refreshToken;
    private static String projectId;
    private static String workItemId;

    @Test
    @Order(1)
    void shouldRegisterUser() throws Exception {
        String body = """
                {
                    "userName": "integrationuser",
                    "emailAddress": "integration@test.com",
                    "password": "password123",
                    "firstName": "Integration",
                    "lastName": "User"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = json.get("token").asText();
        refreshToken = json.get("refreshToken").asText();
    }

    @Test
    @Order(2)
    void shouldLoginWithRegisteredUser() throws Exception {
        String body = """
                {
                    "emailAddress": "integration@test.com",
                    "password": "password123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = json.get("token").asText();
        refreshToken = json.get("refreshToken").asText();
    }

    @Test
    @Order(3)
    void shouldRefreshToken() throws Exception {
        String body = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        MvcResult result = mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = json.get("token").asText();
        refreshToken = json.get("refreshToken").asText();
    }

    @Test
    @Order(4)
    void shouldRejectLoginWithWrongPassword() throws Exception {
        String body = """
                {
                    "emailAddress": "integration@test.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void shouldCreateProject() throws Exception {
        String body = """
                {
                    "name": "Integration Project",
                    "code": "INT",
                    "description": "Test project"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        projectId = objectMapper.readTree(result.getResponse().getContentAsString()).asText();
    }

    @Test
    @Order(6)
    void shouldGetAllProjects() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Integration Project"))
                .andExpect(jsonPath("$[0].code").value("INT"));
    }

    @Test
    @Order(7)
    void shouldGetProjectDetails() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Project"));
    }

    @Test
    @Order(8)
    void shouldCreateWorkItem() throws Exception {
        String body = """
                {
                    "description": "Integration work item",
                    "priority": 1,
                    "type": "Task",
                    "status": "New",
                    "projectId": "%s"
                }
                """.formatted(projectId);

        MvcResult result = mockMvc.perform(post("/api/work-items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        workItemId = objectMapper.readTree(result.getResponse().getContentAsString()).asText();
    }

    @Test
    @Order(9)
    void shouldGetWorkItemById() throws Exception {
        mockMvc.perform(get("/api/work-items/" + workItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Integration work item"))
                .andExpect(jsonPath("$.status").value("New"));
    }

    @Test
    @Order(10)
    void shouldGetWorkItemsByProject() throws Exception {
        mockMvc.perform(get("/api/work-items")
                        .param("projectId", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].description").value("Integration work item"));
    }

    @Test
    @Order(11)
    void shouldUpdateWorkItemStatus() throws Exception {
        String body = """
                {
                    "status": "Active"
                }
                """;

        mockMvc.perform(patch("/api/work-items/" + workItemId + "/status")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // Verify status changed
        mockMvc.perform(get("/api/work-items/" + workItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    @Order(12)
    void shouldUpdateWorkItemNotes() throws Exception {
        String body = """
                {
                    "notes": "Some integration notes"
                }
                """;

        mockMvc.perform(patch("/api/work-items/" + workItemId + "/notes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/work-items/" + workItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.notes").value("Some integration notes"));
    }

    @Test
    @Order(13)
    void shouldDeleteWorkItem() throws Exception {
        mockMvc.perform(delete("/api/work-items/" + workItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/work-items/" + workItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(14)
    void shouldDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }
}
