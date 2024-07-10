package com.xy.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.xy.common.core.utils.ServletUtils;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 自定义限流异常处理
 * Custom handler for handling rate limiting exceptions
 *
 * @author ruoyi
 */
public class SentinelFallbackHandler implements WebExceptionHandler
{
    /**
     * 写入响应
     * Writes the response
     *
     * @param response 响应对象
     * @param exchange 服务器交换对象
     * @return 返回写入响应的Mono
     */
    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange)
    {
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), "请求超过最大数，请稍候再试"); // 调用Servlet工具类写入响应
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex)
    {
        if (exchange.getResponse().isCommitted())
        {
            return Mono.error(ex); // 如果响应已提交，直接抛出异常
        }
        if (!BlockException.isBlockException(ex))
        {
            return Mono.error(ex); // 如果不是BlockException，直接抛出异常
        }
        // 处理被限制的请求，并写入响应
        return handleBlockedRequest(exchange, ex).flatMap(response -> writeResponse(response, exchange));
    }

    /**
     * 处理被限制的请求
     * Handles the blocked request
     *
     * @param exchange  服务器交换对象
     * @param throwable 异常对象
     * @return 返回处理后的ServerResponse的Mono
     */
    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        // 调用GatewayCallbackManager处理被限制的请求

        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }
}
