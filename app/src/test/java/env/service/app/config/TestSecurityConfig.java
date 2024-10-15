package env.service.app.config;

import static org.springframework.security.config.Customizer.withDefaults;

import env.service.app.util.ConstantUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@Profile("springboottest")
public class TestSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/tests/ping").permitAll().anyRequest().authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(withDefaults())
        .build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    final UserDetails user =
        User.withUsername(ConstantUtils.AUTH_USR)
            .password("{noop}".concat(ConstantUtils.AUTH_PWD))
            .roles("USER")
            .build();
    return new InMemoryUserDetailsManager(user);
  }
}
