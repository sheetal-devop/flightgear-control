package org.jason.fgcontrol.gateway.app.poc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
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
	
	// TODO: change to distribution location
	private final static String USER_STORE = "src/main/resources/users.properties";
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
				"/helloworld/*",
				"/actuator/shutdown"
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

	/**
	 * Load the users from the user store. Works with hardcoded user but not ones loaded from store
	 * 
	 * TODO: ^^^^ fix
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	public InMemoryUserDetailsManager userDetailsService() throws Exception {

		LOGGER.debug("Gateway userDetailsService invoked");
		
		//TODO: load users from userstore

		ArrayList<UserDetails> users = null;

		InMemoryUserDetailsManager userDetailsManager = null;
		
		Properties userlist = new Properties();
		try {
			userlist.load(new InputStreamReader(new FileInputStream(USER_STORE)));
			
			users = new ArrayList<>();
			
			//userlist should be a list of users mapped to hash strings
			
			String iUsername, iUserAuthHash, iUsernameCheck;
			for( Entry<Object, Object> userEntry : userlist.entrySet()) {
				//TODO: load into list
				iUsername = String.valueOf(userEntry.getKey());
				
				if(iUsername == null || StringUtils.isEmpty(iUsername)) {
					LOGGER.warn("Failed to read username from store");
				} else {
				
					LOGGER.debug("Loading user {}", iUsername);
					
					iUserAuthHash = String.valueOf(userEntry.getValue());
					if(iUserAuthHash == null || StringUtils.isEmpty(iUserAuthHash)) {
						LOGGER.warn("Failed to read user auth hash from store");
					}
					else {
						
						//require full hash string: fgctl:$2y$08$derphash
						
						int hashDelimIndex = iUserAuthHash.indexOf(':');
						
						iUsernameCheck = iUserAuthHash.substring(0, hashDelimIndex);
						
						if(iUsername.equals(iUsernameCheck)) {
							iUserAuthHash = iUserAuthHash.substring(hashDelimIndex + 1);

							//LOGGER.debug("Read user hash {}", iUserAuthHash);

							users.add(
								User.withUsername(iUsername)
									.password( iUserAuthHash )
									.roles(GATEWAY_USER_ROLE)
									.build()
							);
							
							iUserAuthHash = null;
							
							LOGGER.debug("Loaded user {} successfully", iUsername);
						} else {
							LOGGER.error("User check failed");
						}
					}
				}
			}
			
			// hardcoded users	
			
			UserDetails user = 
				User.withUsername(TEST_USER)
					.password(
						passwordEncoder().encode(TEST_PASS)
					)
					.roles(GATEWAY_USER_ROLE)
					.build();

			users.add(user);
			
			int loadedUserCount = users.size();
			
			LOGGER.debug("Loaded {} users from user store", loadedUserCount);
			
			if(loadedUserCount == 0) {
				throw new Exception("No users loaded");
			}
			
			userDetailsManager = new InMemoryUserDetailsManager(users);
			
		} catch (IOException e) {
			LOGGER.error("IOException loading userlist", e);
			throw e;
		} catch (Exception e) {
			LOGGER.error("Exception loading userlist", e);
			throw e;
		}
		finally {
			if(userDetailsManager == null) {
				LOGGER.error("Failed to load users from user store");	
			} else {
				LOGGER.info("Loaded users from user store");
			}
		}
		
		LOGGER.debug("Gateway userDetailsService returning");
		
		return userDetailsManager;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(HASH_STREN);
	}
}