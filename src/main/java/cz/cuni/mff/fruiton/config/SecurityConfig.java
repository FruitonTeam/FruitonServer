package cz.cuni.mff.fruiton.config;

import cz.cuni.mff.fruiton.component.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    private final TokenAuthenticationFilter tokenFilter;

    @Autowired
    public SecurityConfig(final UserDetailsService userDetailsService, final TokenAuthenticationFilter tokenFilter) {
        this.userDetailsService = userDetailsService;
        this.tokenFilter = tokenFilter;
    }

    @Override
    protected final void configure(final HttpSecurity http) throws Exception {
        http.addFilterBefore(tokenFilter, BasicAuthenticationFilter.class);

        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/secured/**").authenticated()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/avatar/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/img/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/socket*").permitAll()
                .antMatchers("/register").permitAll()
                .antMatchers("/registerWeb").permitAll()
                .antMatchers("/renewPassword").permitAll()
                .antMatchers("/registerGoogle").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/loginFail")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Override
    protected final UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
