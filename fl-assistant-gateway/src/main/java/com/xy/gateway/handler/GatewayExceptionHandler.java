package com.xy.gateway.handler;

import com.xy.common.core.utils.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一异常处理
 * Gateway global exception handler
 *
 * @author ruoyi
 */
@Order(-1) // 定义处理顺序，越小优先级越高
@Configuration // 声明为Spring的配置类
public class GatewayExceptionHandler implements ErrorWebExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class); // 日志记录器

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex)
    {
        ServerHttpResponse response = exchange.getResponse();// 获取响应对象

        if (exchange.getResponse().isCommitted())
        {
            return Mono.error(ex); // 如果响应已提交，直接抛出异常
        }

        String msg;

        if (ex instanceof NotFoundException)
        {
            msg = "服务未找到"; // 如果是NotFoundException，则设置消息为"服务未找到"
        }
        else if (ex instanceof ResponseStatusException)
        {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            msg = responseStatusException.getMessage(); // 如果是ResponseStatusException，则获取异常消息
        }
        else
        {
            msg = "内部服务器错误"; // 其他情况设置消息为"内部服务器错误"
        }

        log.error("[网关异常处理]请求路径:{},异常信息:{}", exchange.getRequest().getPath(), ex.getMessage()); // 记录错误日志

        return ServletUtils.webFluxResponseWriter(response, msg); // 使用Servlet工具类写入响应
    }
}
