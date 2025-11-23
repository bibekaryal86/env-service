package env.service.app.controller;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoInternalException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import env.service.app.config.TestSecurityConfig;
import env.service.app.model.EnvDetails;
import env.service.app.model.EnvDetailsResponse;
import env.service.app.util.ConstantUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("springboottest")
@Import({TestSecurityConfig.class})
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
@AutoConfigureMockMvc
public class EnvDetailsControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private MongoTemplate mongoTemplate;

  private final String basicAuthCredentialsForTest =
      Base64.getEncoder()
          .encodeToString(
              String.format("%s:%s", ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)
                  .getBytes(StandardCharsets.UTF_8));

  private final String TEST_COLLECTION_NAME = "test_collection";
  private final String TEST_DOCUMENT_ID = "test_id";
  private final EnvDetails ENV_DETAILS_REQUEST =
      EnvDetails.builder()
          .name("test_name")
          .stringValue("test_string_value")
          .listValue(List.of("test_list_value_1", "test_list_value_2"))
          .mapValue(Map.of("test_key_1", "test_value_1", "test_key_2", "test_value_2"))
          .build();
  private final EnvDetails ENV_DETAILS_IN_RESPONSE =
      EnvDetails.builder()
          .id(new ObjectId())
          .name(ENV_DETAILS_REQUEST.getName())
          .stringValue(ENV_DETAILS_REQUEST.getStringValue())
          .listValue(ENV_DETAILS_REQUEST.getListValue())
          .mapValue(ENV_DETAILS_REQUEST.getMapValue())
          .build();

  private ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper;
  }

  @AfterEach
  void resetMocks() {
    Mockito.reset(mongoTemplate);
  }

  @Test
  void test_GetAllAppNames_Success() throws Exception {
    when(mongoTemplate.getCollectionNames()).thenReturn(Set.of("app_one", "app_two", "app_three"));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/v1/appNames")
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isOk())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getErrMsg());
    assertNotNull(envDetailsResponse.getEnvDetails());
    assertEquals(1, envDetailsResponse.getEnvDetails().size());
    assertEquals("app_names", envDetailsResponse.getEnvDetails().getFirst().getName());
    assertTrue(envDetailsResponse.getEnvDetails().getFirst().getListValue().contains("one"));
    assertTrue(envDetailsResponse.getEnvDetails().getFirst().getListValue().contains("two"));
    assertTrue(envDetailsResponse.getEnvDetails().getFirst().getListValue().contains("three"));
  }

  @Test
  void test_GetAllAppNames_Failure_Unauthorized() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/appNames")
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, "invalid_password")))
        .andExpect(status().isUnauthorized())
        .andReturn();
  }

  @Test
  void test_GetAllAppNames_Failure_Exception() throws Exception {
    when(mongoTemplate.getCollectionNames())
        .thenThrow(new MongoInternalException("Mongo Internal Exception"));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/v1/appNames")
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isInternalServerError())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getEnvDetails());
    assertNotNull(envDetailsResponse.getErrMsg());
    assertEquals(
        "Look App Names Exception: Mongo Internal Exception", envDetailsResponse.getErrMsg());
  }

  @Test
  void test_DeleteEmptyAppNames_Success() throws Exception {
    when(mongoTemplate.getCollectionNames()).thenReturn(Set.of("app_one", "app_two", "app_three"));
    when(mongoTemplate.getCollection("app_one")).thenReturn(mock(MongoCollection.class));
    when(mongoTemplate.getCollection("app_two")).thenReturn(mock(MongoCollection.class));
    when(mongoTemplate.getCollection("app_three")).thenReturn(mock(MongoCollection.class));
    when(mongoTemplate.getCollection("app_one").countDocuments()).thenReturn(0L);
    when(mongoTemplate.getCollection("app_two").countDocuments()).thenReturn(1L);
    when(mongoTemplate.getCollection("app_three").countDocuments()).thenReturn(2L);

    mockMvc
        .perform(
            delete("/api/v1/appNames")
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
        .andExpect(status().isOk())
        .andReturn();

    verify(mongoTemplate).dropCollection("app_one");
    verify(mongoTemplate, never()).dropCollection("app_two");
    verify(mongoTemplate, never()).dropCollection("app_three");
  }

  @Test
  void test_DeleteEmptyAppNames_Failure_Unauthorized() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/appNames")
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, "invalid_password")))
        .andExpect(status().isUnauthorized())
        .andReturn();
  }

  @Test
  void test_DeleteEmptyAppNames_Failure_Exception() throws Exception {
    when(mongoTemplate.getCollectionNames())
        .thenThrow(new MongoInternalException("Mongo Internal Exception"));

    MvcResult mvcResult =
        mockMvc
            .perform(
                delete("/api/v1/appNames")
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isInternalServerError())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getEnvDetails());
    assertNotNull(envDetailsResponse.getErrMsg());
    assertEquals(
        "Delete Empty App Names Exception: Mongo Internal Exception",
        envDetailsResponse.getErrMsg());
  }

  @Test
  void test_Create_Success() throws Exception {
    when(mongoTemplate.save(any(EnvDetails.class), eq("app_" + TEST_COLLECTION_NAME)))
        .thenReturn(ENV_DETAILS_IN_RESPONSE);

    String requestBody = objectMapper().writeValueAsString(ENV_DETAILS_REQUEST);

    MvcResult mvcResult =
        mockMvc
            .perform(
                post(String.format("/api/v1/%s", TEST_COLLECTION_NAME))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isOk())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getErrMsg());
    assertNotNull(envDetailsResponse.getEnvDetails());
    assertEquals(envDetailsResponse.getEnvDetails().size(), 1);
    assertEquals(
        ENV_DETAILS_REQUEST.getName(), envDetailsResponse.getEnvDetails().getFirst().getName());
  }

  @Test
  void test_Create_Failure_Unauthorized() throws Exception {
    String requestBody = objectMapper().writeValueAsString(ENV_DETAILS_REQUEST);

    mockMvc
        .perform(
            post(String.format("/api/v1/%s", TEST_COLLECTION_NAME))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, "invalid_password")))
        .andExpect(status().isUnauthorized())
        .andReturn();
  }

  @Test
  void test_Create_Failure_ValidationError_Name() throws Exception {
    ENV_DETAILS_REQUEST.setName(null);
    String requestBody = objectMapper().writeValueAsString(ENV_DETAILS_REQUEST);

    MvcResult mvcResult =
        mockMvc
            .perform(
                post(String.format("/api/v1/%s", TEST_COLLECTION_NAME))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isBadRequest())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getEnvDetails());
    assertNotNull(envDetailsResponse.getErrMsg());
    assertTrue(envDetailsResponse.getErrMsg().contains("Env Details Validation Error"));

    // reset
    ENV_DETAILS_REQUEST.setName(ENV_DETAILS_IN_RESPONSE.getName());
  }

  @Test
  void test_Create_Failure_ValidationError_Values() throws Exception {
    ENV_DETAILS_REQUEST.setStringValue(null);
    ENV_DETAILS_REQUEST.setListValue(Collections.emptyList());
    ENV_DETAILS_REQUEST.setMapValue(Collections.emptyMap());

    String requestBody = objectMapper().writeValueAsString(ENV_DETAILS_REQUEST);

    MvcResult mvcResult =
        mockMvc
            .perform(
                post(String.format("/api/v1/%s", TEST_COLLECTION_NAME))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isBadRequest())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getEnvDetails());
    assertNotNull(envDetailsResponse.getErrMsg());
    assertTrue(envDetailsResponse.getErrMsg().contains("Env Details Validation Error"));

    // reset
    ENV_DETAILS_REQUEST.setStringValue(ENV_DETAILS_IN_RESPONSE.getStringValue());
    ENV_DETAILS_REQUEST.setListValue(ENV_DETAILS_IN_RESPONSE.getListValue());
    ENV_DETAILS_REQUEST.setMapValue(ENV_DETAILS_IN_RESPONSE.getMapValue());
  }

  @Test
  void test_Read_Success() throws Exception {
    when(mongoTemplate.findAll(eq(EnvDetails.class), eq("app_" + TEST_COLLECTION_NAME)))
        .thenReturn(List.of(ENV_DETAILS_IN_RESPONSE));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get(String.format("/api/v1/%s", TEST_COLLECTION_NAME))
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isOk())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getErrMsg());
    assertNotNull(envDetailsResponse.getEnvDetails());
    assertEquals(envDetailsResponse.getEnvDetails().size(), 1);
    assertEquals(
        ENV_DETAILS_REQUEST.getName(), envDetailsResponse.getEnvDetails().getFirst().getName());
  }

  @Test
  void test_Read_Failure_Unauthorized() throws Exception {
    mockMvc
        .perform(
            get(String.format("/api/v1/%s", TEST_COLLECTION_NAME))
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, "invalid_password")))
        .andExpect(status().isUnauthorized())
        .andReturn();
  }

  @Test
  void test_Read_Failure_Exception() throws Exception {
    when(mongoTemplate.findAll(eq(EnvDetails.class), eq("app_" + TEST_COLLECTION_NAME)))
        .thenThrow(new MongoInternalException("Mongo Internal Exception"));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get(String.format("/api/v1/%s", TEST_COLLECTION_NAME))
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isInternalServerError())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getEnvDetails());
    assertNotNull(envDetailsResponse.getErrMsg());
    assertEquals("Read Exception: Mongo Internal Exception", envDetailsResponse.getErrMsg());
  }

  @Test
  void test_Update_Failure_NotAllowed() throws Exception {
    String requestBody = objectMapper().writeValueAsString(ENV_DETAILS_REQUEST);

    MvcResult mvcResult =
        mockMvc
            .perform(
                put(String.format("/api/v1/%s/%s", TEST_COLLECTION_NAME, TEST_DOCUMENT_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getEnvDetails());
    assertNotNull(envDetailsResponse.getErrMsg());
    assertEquals(
        "Update Exception! Update not available!! Delete and Create!!!",
        envDetailsResponse.getErrMsg());
  }

  @Test
  void test_Update_Failure_Unauthorized() throws Exception {
    String requestBody = objectMapper().writeValueAsString(ENV_DETAILS_REQUEST);

    mockMvc
        .perform(
            put(String.format("/api/v1/%s/%s", TEST_COLLECTION_NAME, TEST_DOCUMENT_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, "invalid_password")))
        .andExpect(status().isUnauthorized())
        .andReturn();
  }

  @Test
  void test_Delete_Success() throws Exception {
    when(mongoTemplate.remove(
            eq(new Query(Criteria.where("name").is(ENV_DETAILS_REQUEST.getName()))),
            eq("app_" + TEST_COLLECTION_NAME)))
        .thenReturn(DeleteResult.acknowledged(1));

    mockMvc
        .perform(
            delete(
                    String.format(
                        "/api/v1/%s/%s", TEST_COLLECTION_NAME, ENV_DETAILS_REQUEST.getName()))
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  void test_Delete_Failure_Unauthorized() throws Exception {
    mockMvc
        .perform(
            delete(
                    String.format(
                        "/api/v1/%s/%s", TEST_COLLECTION_NAME, ENV_DETAILS_REQUEST.getName()))
                .with(
                    SecurityMockMvcRequestPostProcessors.httpBasic(
                        ConstantUtils.AUTH_USR, "invalid_password")))
        .andExpect(status().isUnauthorized())
        .andReturn();
  }

  @Test
  void test_Delete_Failure_Exception() throws Exception {
    when(mongoTemplate.remove(
            eq(new Query(Criteria.where("name").is(ENV_DETAILS_REQUEST.getName()))),
            eq("app_" + TEST_COLLECTION_NAME)))
        .thenThrow(new MongoInternalException("Mongo Internal Exception"));

    MvcResult mvcResult =
        mockMvc
            .perform(
                delete(
                        String.format(
                            "/api/v1/%s/%s", TEST_COLLECTION_NAME, ENV_DETAILS_REQUEST.getName()))
                    .with(
                        SecurityMockMvcRequestPostProcessors.httpBasic(
                            ConstantUtils.AUTH_USR, ConstantUtils.AUTH_PWD)))
            .andExpect(status().isInternalServerError())
            .andReturn();

    EnvDetailsResponse envDetailsResponse =
        objectMapper()
            .readValue(mvcResult.getResponse().getContentAsString(), EnvDetailsResponse.class);

    assertNotNull(envDetailsResponse);
    assertNull(envDetailsResponse.getEnvDetails());
    assertNotNull(envDetailsResponse.getErrMsg());
    assertEquals("Delete Exception: Mongo Internal Exception", envDetailsResponse.getErrMsg());
  }
}
