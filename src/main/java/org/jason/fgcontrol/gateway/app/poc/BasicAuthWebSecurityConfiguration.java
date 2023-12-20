package org.jason.fgcontrol.gateway.app.poc;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BasicAuthWebSecurityConfiguration {

	private final static Logger LOGGER = LoggerFactory.getLogger(BasicAuthWebSecurityConfiguration.class);
	
	private final static String USER_STORE = "users.properties";
	private final static String GATEWAY_USER_ROLE = "USER_ROLE";
	
	// TODO: migrate to reading hashes from file
	private final static String TEST_USER = "user";
	private final static String TEST_PASS = "password";
	
	private final static int HASH_STREN = 8;
	
	@Autowired
	private MyBasicAuthenticationEntryPoint authenticationEntryPoint;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		LOGGER.debug("Gateway filterChain invoked");
			
		http
			.csrf()
			.disable()
			.authorizeRequests()
			.antMatchers(
				"/helloworld/*"
			)
			.permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.httpBasic()
			.authenticationEntryPoint(authenticationEntryPoint);

		LOGGER.debug("Gateway filterChain returning");
		
		return http.build();
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {

		LOGGER.debug("Gateway userDetailsService invoked");
		
		//TODO: load users from userstore

		ArrayList<UserDetails> users = new ArrayList<>();

		UserDetails user = 
			User.withUsername(TEST_USER)
				.password(
					passwordEncoder().encode(TEST_PASS)
				)
				.roles(GATEWAY_USER_ROLE)
				.build();

		users.add(user);

		LOGGER.debug("Gateway userDetailsService returning");
		
		return new InMemoryUserDetailsManager(users);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(HASH_STREN);
	}
}