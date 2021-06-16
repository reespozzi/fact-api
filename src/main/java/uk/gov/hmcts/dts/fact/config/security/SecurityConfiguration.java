package uk.gov.hmcts.dts.fact.config.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(a -> a
                .antMatchers(HttpMethod.GET, "/admin/**").authenticated()
                .antMatchers(HttpMethod.GET, "/courts/").authenticated()
                .antMatchers(HttpMethod.GET, "/courts/all").authenticated()
                .antMatchers(HttpMethod.GET, "/courts/{slug}/*").authenticated()
                .antMatchers(HttpMethod.PUT, "/*").authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        // Allow POST requests that retrieve but not create anything
        // to accept a request without a bearer token
        http.csrf().ignoringAntMatchers("/validate/postcodes");
    }

    @Bean
    public RolesProvider rolesProvider() {
        return new SpringRolesProvider();
    }

}
