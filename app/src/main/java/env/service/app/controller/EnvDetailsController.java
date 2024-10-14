package env.service.app.controller;

import static env.service.app.util.CommonUtils.getAppCollectionName;

import env.service.app.model.EnvDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<EnvDetails> create(
      @PathVariable final String appName, @RequestBody final EnvDetails envDetails) {
    try {
      return ResponseEntity.ok(mongoTemplate.save(envDetails, getAppCollectionName(appName)));
    } catch (Exception ex) {
      log.error("Create Exception: [{}] | [{}]", appName, envDetails, ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/{appName}")
  public ResponseEntity<List<EnvDetails>> read(@PathVariable final String appName) {
    try {
      return ResponseEntity.ok(
          mongoTemplate.findAll(EnvDetails.class, getAppCollectionName(appName)));
    } catch (Exception ex) {
      log.error("Read Exception: [{}]", appName, ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PutMapping("/{appName}/{id}")
  public ResponseEntity<EnvDetails> update(
      @PathVariable final String appName,
      @PathVariable final String id,
      @RequestBody final EnvDetails envDetails) {
    log.error("Update Exception! Update not available!! Delete and Create!!!");
    return ResponseEntity.unprocessableEntity().build();
  }

  @DeleteMapping("/{appName}/{id}")
  public ResponseEntity<EnvDetails> delete(
      @PathVariable final String appName, @PathVariable final String id) {
    try {
      mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), getAppCollectionName(appName));
      return ResponseEntity.ok().build();
    } catch (Exception ex) {
      log.error("Delete Exception: [{}] | [{}]", appName, id, ex);
      return ResponseEntity.internalServerError().build();
    }
  }
}
