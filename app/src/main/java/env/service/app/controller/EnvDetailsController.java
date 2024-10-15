package env.service.app.controller;

import static env.service.app.util.CommonUtils.getAppCollectionName;

import env.service.app.model.EnvDetails;
import env.service.app.model.EnvDetailsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class EnvDetailsController {

  private final MongoTemplate mongoTemplate;

  @PostMapping("/{appName}")
  public ResponseEntity<EnvDetailsResponse> create(
      @PathVariable final String appName, @RequestBody final EnvDetails envDetails) {
    try {
      if (!validateEnvDetails(appName, envDetails)) {
        log.error("Env Details Validation Error: [{}] | [{}]", appName, envDetails.getName());
        return ResponseEntity.badRequest()
            .body(EnvDetailsResponse.builder().errMsg("Env Details Validation Error").build());
      }
      EnvDetails envDetailsSaved = mongoTemplate.save(envDetails, getAppCollectionName(appName));
      return ResponseEntity.ok(
          EnvDetailsResponse.builder().envDetails(List.of(envDetailsSaved)).build());
    } catch (Exception ex) {
      log.error("Create Exception: [{}] | [{}]", appName, envDetails, ex);
      return ResponseEntity.internalServerError()
          .body(
              EnvDetailsResponse.builder().errMsg("Create Exception: " + ex.getMessage()).build());
    }
  }

  @GetMapping("/{appName}")
  public ResponseEntity<EnvDetailsResponse> read(@PathVariable final String appName) {
    try {
      List<EnvDetails> envDetailsList =
          mongoTemplate.findAll(EnvDetails.class, getAppCollectionName(appName));
      return ResponseEntity.ok(EnvDetailsResponse.builder().envDetails(envDetailsList).build());
    } catch (Exception ex) {
      log.error("Read Exception: [{}]", appName, ex);
      return ResponseEntity.internalServerError()
          .body(EnvDetailsResponse.builder().errMsg("Read Exception: " + ex.getMessage()).build());
    }
  }

  @PutMapping("/{appName}/{id}")
  public ResponseEntity<EnvDetailsResponse> update(
      @PathVariable final String appName,
      @PathVariable final String id,
      @RequestBody final EnvDetails envDetails) {
    log.error("Update Exception! Update not available!! Delete and Create!!!");
    return new ResponseEntity<>(
        EnvDetailsResponse.builder()
            .errMsg("Update Exception! Update not available!! Delete and Create!!!")
            .build(),
        HttpStatus.METHOD_NOT_ALLOWED);
  }

  @DeleteMapping("/{appName}/{envDetailsName}")
  public ResponseEntity<EnvDetailsResponse> delete(
      @PathVariable final String appName, @PathVariable final String envDetailsName) {
    try {
      mongoTemplate.remove(
          new Query(Criteria.where("name").is(envDetailsName)), getAppCollectionName(appName));
      return ResponseEntity.ok().build();
    } catch (Exception ex) {
      log.error("Delete Exception: [{}] | [{}]", appName, envDetailsName, ex);
      return ResponseEntity.internalServerError()
          .body(
              EnvDetailsResponse.builder().errMsg("Delete Exception: " + ex.getMessage()).build());
    }
  }

  private boolean validateEnvDetails(final String appName, final EnvDetails envDetails) {
    // name is required
    if (!StringUtils.hasText(envDetails.getName())) {
      log.error("Env Details Name is missing: [{}]", appName);
      return false;
    }
    // either string value, list value or map value is required
    if (!StringUtils.hasText(envDetails.getStringValue())
        && CollectionUtils.isEmpty(envDetails.getListValue())
        && CollectionUtils.isEmpty(envDetails.getMapValue())) {
      log.error("Env Details Values are missing: [{}] | [{}]", appName, envDetails.getName());
      return false;
    }
    // do not allow to save multiple documents with same name
    List<EnvDetails> envDetailsList =
        mongoTemplate.findAll(EnvDetails.class, getAppCollectionName(appName));

    if (envDetailsList.isEmpty()) {
      return true;
    }

    return envDetailsList.stream()
            .filter(envDetail -> envDetail.getName().equals(envDetails.getName()))
            .findFirst()
            .orElse(null)
        == null;
  }
}
