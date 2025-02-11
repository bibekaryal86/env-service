/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package env.service;

import static env.service.app.util.CommonUtils.getSystemEnvProperty;
import static env.service.app.util.ConstantUtils.AUTH_PWD;
import static env.service.app.util.ConstantUtils.AUTH_USR;
import static env.service.app.util.ConstantUtils.MONGO_APP;
import static env.service.app.util.ConstantUtils.MONGO_DB;
import static env.service.app.util.ConstantUtils.MONGO_PWD;
import static env.service.app.util.ConstantUtils.MONGO_USR;
import static env.service.app.util.ConstantUtils.SERVER_PORT;
import static java.util.Collections.singletonMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class App {

  public static void main(final String[] args) {
    log.info("Begin application initialization...");
    validateEnvVarsInput();

    SpringApplication app = new SpringApplication(App.class);
    app.setDefaultProperties(
        singletonMap("server.port", getSystemEnvProperty(SERVER_PORT, "8002")));
    app.run(args);
    log.info("End application initialization...");
  }

  private static void validateEnvVarsInput() {
    boolean isEnvVarsMissing = getSystemEnvProperty(AUTH_USR, null) == null;
    if (getSystemEnvProperty(AUTH_PWD, null) == null) {
      isEnvVarsMissing = true;
    }
    if (getSystemEnvProperty(MONGO_APP, null) == null) {
      isEnvVarsMissing = true;
    }
    if (getSystemEnvProperty(MONGO_DB, null) == null) {
      isEnvVarsMissing = true;
    }
    if (getSystemEnvProperty(MONGO_USR, null) == null) {
      isEnvVarsMissing = true;
    }
    if (getSystemEnvProperty(MONGO_PWD, null) == null) {
      isEnvVarsMissing = true;
    }
    if (isEnvVarsMissing) {
      throw new IllegalStateException(
          "One or more required env variables are missing for initialization...");
    }
  }
}
