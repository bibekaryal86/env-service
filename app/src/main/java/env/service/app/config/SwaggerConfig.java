package env.service.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Env Service")
                .description("Service to manage runtime variables for other applications")
                .contact(new Contact().name("Bibek Aryal"))
                .license(new License().name("Not for commercial use"))
                .version("1.0.1"));
  }
}
