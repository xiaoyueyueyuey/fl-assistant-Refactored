package com.xy.gateway.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import springfox.documentation.swagger.web.*;

import java.util.Optional;

/**
 * Swagger资源处理器
 * Handles Swagger resources
 */
@RestController
@RequestMapping("/swagger-resources")
public class SwaggerHandler
{
    private final SwaggerResourcesProvider swaggerResources; // Swagger资源提供者
    @Autowired(required = false)
    private SecurityConfiguration securityConfiguration; // 可选的安全配置
    @Autowired(required = false)
    private UiConfiguration uiConfiguration; // 可选的UI配置

    @Autowired
    public SwaggerHandler(SwaggerResourcesProvider swaggerResources)
    {
        this.swaggerResources = swaggerResources;
    }

    /**
     * 获取安全配置
     * Get security configuration
     *
     * @return 返回安全配置的Mono响应实体
     */
    @GetMapping("/configuration/security")
    public Mono<ResponseEntity<SecurityConfiguration>> securityConfiguration()
    {
        return Mono.just(new ResponseEntity<>(
                Optional.ofNullable(securityConfiguration).orElse(SecurityConfigurationBuilder.builder().build()),
                HttpStatus.OK));
    }

    /**
     * 获取UI配置
     * Get UI configuration
     *
     * @return 返回UI配置的Mono响应实体
     */
    @GetMapping("/configuration/ui")
    public Mono<ResponseEntity<UiConfiguration>> uiConfiguration()
    {
        return Mono.just(new ResponseEntity<>(
                Optional.ofNullable(uiConfiguration).orElse(UiConfigurationBuilder.builder().build()), HttpStatus.OK));
    }

    /**
     * 获取Swagger资源
     * Get Swagger resources
     *
     * @return 返回Swagger资源的Mono响应实体
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("")
    public Mono<ResponseEntity> swaggerResources()
    {
        return Mono.just((new ResponseEntity<>(swaggerResources.get(), HttpStatus.OK)));
    }
}
