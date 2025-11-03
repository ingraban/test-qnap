package name.saak.test.app.eml;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/eml/**", "/css/**", "/js/**").permitAll() // alles offen; bei Bedarf anpassen
				.anyRequest().authenticated())
				.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.headers(headers -> headers.contentSecurityPolicy(csp -> getPolicyDirectives(csp)).xssProtection(Customizer.withDefaults())
						.referrerPolicy(
								ref -> ref.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
						.frameOptions(frame -> frame.deny()).httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
						.contentTypeOptions(Customizer.withDefaults()))
				// keine Login-Seite nötig; wenn Auth später gewünscht, hier .formLogin() ergänzen
				.httpBasic(Customizer.withDefaults());

		return http.build();
	}

	private HeadersConfigurer<HttpSecurity>.ContentSecurityPolicyConfig getPolicyDirectives(HeadersConfigurer<HttpSecurity>.ContentSecurityPolicyConfig csp) {
		return csp.policyDirectives("""
				default-src 'none';
				img-src 'self' data:;
				style-src 'self' 'unsafe-inline';
				form-action 'self';
				base-uri 'none';
				object-src 'none';
				frame-ancestors 'none';
				""");
	}

	@Bean
	public UserDetailsService users() {
		var user = User.withUsername("admin").password("{noop}geheim") // {noop} = kein Hash
				.roles("ADMIN").build();
		return new InMemoryUserDetailsManager(user);
	}
}