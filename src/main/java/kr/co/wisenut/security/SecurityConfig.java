package kr.co.wisenut.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
    LoginIdPwValidator loginIdPwValidator;
	
    @Autowired
    TMSimpleLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    TMSimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler;
    
    @Override
	protected void configure(HttpSecurity http) throws Exception {
		http.headers().contentSecurityPolicy("script-src 'self' 'unsafe-inline'");

        // for https
        http.headers()
        	.httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000).preload(true);

        http.csrf().disable()
        		.authorizeRequests()
				.antMatchers("/", "/register", "/user/register").permitAll()
				.antMatchers("/login", "/logout/invalid", "/logout/expired").permitAll()
				.antMatchers("/css/**", "/font/**", "/img/**", "/lib/**", "/font-awesome/**").permitAll() 		// static resource
//				.antMatchers("/docs/**" ).authenticated() 														// static/docs/manual-pdf
				.antMatchers("/actuator/**").hasAuthority("ADMIN") // FIXME(winst65) for spring-boot-develop-tool
				.antMatchers("/api/**").authenticated()
				.antMatchers("/users/**", "/mng/user/users.js").hasAuthority("ADMIN")
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/login")
				.usernameParameter("userId")
				.passwordParameter("userPw")
				.defaultSuccessUrl("/dashboard", true)
				.successHandler(authenticationSuccessHandler)
				.and()
			.logout()
				.logoutSuccessHandler(logoutSuccessHandler)
				.invalidateHttpSession(true) // TODO(wisnt65) session invalidate 구현 필요
//                .deleteCookies("JSESSIONID")
				.permitAll()
				.and()
			.httpBasic(); // HttpBasic or FormLogin

        http.sessionManagement()
        	.maximumSessions(1)
    		.sessionRegistry(sessionRegistry())
    		.expiredUrl("/logout/expired") //invalid session 에 묻힘
                //.maxSessionsPreventsLogin(true) // true - 중복 로그인 불허, false(default) - 기존 세션 로그아웃
    		.and()
			.invalidSessionUrl("/logout/invalid") // expiredUrl 보다 우선 적용되고 있음
        ;
	}
    
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(loginIdPwValidator);
    }
    
    // Register HttpSessionEventPublisher
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    
    // thymeleaf로 UI 구성 시, SpringSecurityDialect가 생성되게 해야함.
    @Bean
    public SpringSecurityDialect springSecurityDialect() { 
        return new SpringSecurityDialect();
    }
}
