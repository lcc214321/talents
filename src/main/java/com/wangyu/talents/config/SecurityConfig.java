package com.wangyu.talents.config;

import com.wangyu.talents.security.CustomUserSecurityDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author wangyu
 * @Date 2019/3/9 14:11
 * @Version 1.0
 **/
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  /**
   * 忽略权限判断的url
   */
  @Value("${author.ignoreUrls}")
  private String[] ignoreUrls;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // 允许所有用户访问"/"和"/home"等地址
    http
        .authorizeRequests()
        // 系统中的“登录页面”在被访问时，不进行权限控制
        .antMatchers(ignoreUrls).permitAll()
        // 其它访问都要验证权限
        .anyRequest().authenticated().and()
        // ==================== 设定登录页的url地址，它不进行权限控制
        .formLogin()
        // 由于后端提供的都是restful接口，并没有直接跳转的页面
        // 所以只要访问的url没有通过权限认证，就跳到这个请求上，并直接排除权限异常
        .loginPage("/v1/security/loginFail")
        // 登录请求点
        .loginProcessingUrl("/login")
        // 登录失败，返回到这里
        .failureForwardUrl("/v1/security/loginFail")
        // 登录成功后，默认到这个URL，返回登录成功后的信息
        .successForwardUrl("/v1/security/loginSuccess").permitAll().and().headers().frameOptions()
        .disable().and()
        // ===================== 设定登出后的url地址
        .logout()
        // 登出页面
        .logoutUrl("/v1/security/logout")
        // 登录成功后
        .logoutSuccessUrl("/v1/security/logoutSuccess").permitAll().and()
        // ===================== 关闭csrf
        .csrf()
        .disable()
        .rememberMe()
        // 持续化登录，登录时间为100天
        .tokenValiditySeconds(100 * 24 * 60 * 60)
        .rememberMeCookieName("persistence")
        .alwaysRemember(true);
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(customUserDetailsService())
        // 设置密码加密模式
        .passwordEncoder(passwordEncoder());
  }

  /**
   * 用户密码的加密方式为MD5加密
   */
  @Bean
  public Md5PasswordEncoder passwordEncoder() {
    return new Md5PasswordEncoder();

  }

  /**
   * 自定义UserDetailsService，从数据库中读取用户信息
   */
  @Bean
  public CustomUserSecurityDetailsService customUserDetailsService() {
    return new CustomUserSecurityDetailsService();
  }
}
