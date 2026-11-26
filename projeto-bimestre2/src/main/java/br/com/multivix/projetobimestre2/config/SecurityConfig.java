package br.com.multivix.projetobimestre2.config;

import br.com.multivix.projetobimestre2.entity.Usuario;
import br.com.multivix.projetobimestre2.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PrimeiroAcessoFilter primeiroAcessoFilter;

    public SecurityConfig(PrimeiroAcessoFilter primeiroAcessoFilter) {
        this.primeiroAcessoFilter = primeiroAcessoFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler(UsuarioService usuarioService) {
        return (request, response, authentication) -> {
            if (authentication.getPrincipal() instanceof Usuario usuario) {
                usuarioService.registrarUltimoLogin(usuario.getId());
                if (usuario.isPrimeiroAcessoAtivo()) {
                    response.sendRedirect("/primeiro-acesso");
                    return;
                }
            }
            response.sendRedirect("/");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler loginSuccessHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers("/primeiro-acesso").authenticated()
                .requestMatchers("/usuarios/**", "/configuracoes/**", "/inventario/reiniciar")
                    .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/equipamentos/baixar/**")
                    .hasRole("MANAGER")
                .requestMatchers(HttpMethod.GET, "/anexos/download/**")
                    .authenticated()
                .requestMatchers("/anexos/**")
                    .hasAnyRole("MANAGER", "ADMIN", "SECRETARIA", "OPERADOR")
                .requestMatchers("/setores/novo", "/setores/salvar", "/setores/editar/**", "/setores/excluir/**")
                    .hasAnyRole("MANAGER", "ADMIN", "SECRETARIA")
                .requestMatchers("/responsaveis/novo", "/responsaveis/salvar", "/responsaveis/editar/**", "/responsaveis/excluir/**")
                    .hasAnyRole("MANAGER", "ADMIN", "SECRETARIA")
                .requestMatchers("/equipamentos/novo", "/equipamentos/salvar", "/equipamentos/editar/**", "/equipamentos/excluir/**",
                                 "/equipamentos/conferir/**", "/inventario/conferir/**",
                                 "/movimentacoes/nova", "/movimentacoes/salvar", "/movimentacoes/devolver/**",
                                 "/movimentacoes/prorrogar/**", "/movimentacoes/excluir/**")
                    .hasAnyRole("MANAGER", "ADMIN", "SECRETARIA", "OPERADOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?erro=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .addFilterAfter(primeiroAcessoFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}