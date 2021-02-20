package com.example.app.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	 @Bean
	    public UserDetailsService userDetailsService() {
	        return new UserDetailServiceImpl();
	    }
	     
	    @Bean
	    public BCryptPasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	     
	    @Bean
	    public DaoAuthenticationProvider authenticationProvider() {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	        authProvider.setUserDetailsService(userDetailsService());
	        authProvider.setPasswordEncoder(passwordEncoder());
	         
	        return authProvider;
	    }
	 
	    @Override
	    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	        auth.authenticationProvider(authenticationProvider());
	    }
	    

	    @Override
	    protected void configure(HttpSecurity http) throws Exception {
	        http.cors().and().authorizeRequests()
	        	.antMatchers("/senha",
	        			"/password",
	        			"/imagem",
	        			"/emailReset",
	        			"/verificaNovaSenha",
	        			"/novaSenha", 
	        			"/email",
	        			"/usuario" 
	        			,"/imagens/**",
	        			"/css/**",
	        			"/js/**",
	        			"manifest.json").permitAll()
	            .anyRequest().authenticated()
	            .and()
	            .formLogin().loginPage("/login")
	            .usernameParameter("username")
	            .passwordParameter("password")
	            .defaultSuccessUrl("/index", true)
	            .permitAll()
	            .and()
	            .logout().permitAll()
	            .and().sessionManagement().maximumSessions(1).expiredUrl("/cadastro");
	    }
	    
	    @Bean
	    CorsConfigurationSource corsConfigurationSource() 
	    {
	        CorsConfiguration configuration = new CorsConfiguration();
	        configuration.setAllowedOrigins(Arrays.asList("https://localhost:8080"));
	        configuration.setAllowedMethods(Arrays.asList("GET","POST"));
	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	        source.registerCorsConfiguration("/**", configuration);
	        return source;
	    }

}
