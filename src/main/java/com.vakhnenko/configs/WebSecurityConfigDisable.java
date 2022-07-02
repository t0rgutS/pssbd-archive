package com.vakhnenko.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

//@Configuration
//@EnableWebMvcSecurity
//@Profile("!SecurityOn")
//public class WebSecurityConfigDisable extends WebSecurityConfigurerAdapter {

//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//      http
//              .authorizeRequests()
//             .antMatchers("/**").permitAll();
//  }
public class WebSecurityConfigDisable {
}
