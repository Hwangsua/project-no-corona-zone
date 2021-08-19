package com.megait.nocoronazone.configuration;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
<<<<<<< HEAD
                .mvcMatchers("/", "/login", "/signup",
                        "/clinic", "/news", "/vaccine", "/article",
                        "/cosns", "/timeline_location",
                        "/check-email", "/email-check-token",
                        "/xml").permitAll()

                .mvcMatchers("/css/**","/img/**", "/js/**", "/svg/**").permitAll()
=======
                .mvcMatchers("/", "/login", "/signup", "/nicknameCk","/logout","/settings",
                        "/infection", "/density", "/distancing", "/clinic",
                        "/vaccine", "/news",  "/news/article", "/news/video",
                        "/timeline_follow", "/timeline_location","/mention/write","/mention_detail",
                        "/remention", "/search", "/following","/follower","/{nickname}").permitAll()
>>>>>>> 401ff989d92bdb2a280cef7493b52068d9021139

                .mvcMatchers("https://nip.kdca.go.kr/irgd/cov19stats.do?list=all").permitAll()
                .mvcMatchers("https://cdn.jsdelivr.net/gh/projectnoonnu/noonfonts_2105_2@1.0/Cafe24SsurroundAir.woff").permitAll()

                .anyRequest().authenticated()

                .and()
                .formLogin()
                .loginPage("/login")  // 안해도 기본값이 이미 '/login'임
                .defaultSuccessUrl("/", true)

                .and()
                .logout()
                .logoutUrl("/logout") // 안해도 기본값이 이미 '/logout'임임
                .invalidateHttpSession(true) // 로그아웃했을때 세션을 갱신
                .logoutSuccessUrl("/"); // 로그아웃하면 메인으로 가게
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
