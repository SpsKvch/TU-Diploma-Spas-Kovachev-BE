package com.test.security.configs;

import com.test.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


import static com.test.utils.Constants.ADMIN;
import static com.test.utils.Constants.ROOT;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(UriConfigurationProperties.class)
public class SecurityConfiguration {

  private final JwtFilter jwtFilter;
  private final UriConfigurationProperties uriConfigurationProperties;
  @Bean
  @Order(1)
  public SecurityFilterChain workingChainLmao(HttpSecurity http) throws Exception {
    return http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(x -> x.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(x -> x.requestMatchers(uriConfigurationProperties.getPublicEndpoints()).permitAll())
        .authorizeHttpRequests(x -> x.requestMatchers(HttpMethod.GET, uriConfigurationProperties.getProtectedGetEndpoints()).authenticated())
        .authorizeHttpRequests(x -> x.requestMatchers(HttpMethod.GET, "/**").permitAll())
        .authorizeHttpRequests(x -> x.requestMatchers(HttpMethod.POST, "/v1/categories/*").hasAnyAuthority(ROOT, ADMIN))
        .authorizeHttpRequests(x -> x.requestMatchers(HttpMethod.PATCH, "/v1/categories/**").hasAnyAuthority(ROOT, ADMIN))
        .authorizeHttpRequests(x -> x.requestMatchers(HttpMethod.DELETE, "/v1/categories/**").hasAnyAuthority(ROOT, ADMIN))
        .authorizeHttpRequests(x -> x.requestMatchers(HttpMethod.DELETE, "/v1/templates/complete/**").hasAnyAuthority(ROOT, ADMIN))
        .authorizeHttpRequests(x -> x.anyRequest().authenticated())
        .addFilterBefore(jwtFilter, AuthorizationFilter.class)
        .build();
  }


  //@Bean
  //@Order(1)
  public SecurityFilterChain publicApiFilterChain(HttpSecurity http) throws Exception {
    return http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(x -> x.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(x -> x.requestMatchers("/v1/users/login", "/v1/users/register",
            "/swagger-ui/**", "/v3/api-docs/**", "/error/**").permitAll())
        .authorizeHttpRequests(x -> x.anyRequest().authenticated())
        .securityContext(context -> context.securityContextRepository(
            new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository())))
        .build();
  }

  //@Bean
  //@Order(2)
  public SecurityFilterChain authenticationFilterChain(HttpSecurity http) throws Exception {
    return http.cors(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
/*                .authorizeHttpRequests(x -> x.requestMatchers("/v1/users/login", "/v1/users/register",
                        "/swagger-ui/**", "/v3/api-docs/**", "/error/**").permitAll())*/
        .authorizeHttpRequests(x -> x.anyRequest().authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class).build();
  }

  @Bean
  public PasswordEncoder defaultEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    //config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.addExposedHeader("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  //This is a stateful implementation. Requires refresh token as JWT is persisted during the session
//    @Bean
//    @Order(1)
//    public SecurityFilterChain publicApiFilterChain(HttpSecurity http) throws Exception {
//        return http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
//                .exceptionHandling(x -> x.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
//                .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
//                .authorizeHttpRequests(x -> x.requestMatchers("/v1/users/login", "/v1/users/register",
//                        "/swagger-ui/**", "/v3/api-docs/**", "/error/**").permitAll())
//                .authorizeHttpRequests(x -> x.anyRequest().authenticated())
//                .securityContext(context -> context.securityContextRepository(
//                        new DelegatingSecurityContextRepository(
//                                new RequestAttributeSecurityContextRepository(),
//                                new HttpSessionSecurityContextRepository())))
//                .build();
//    }


}
