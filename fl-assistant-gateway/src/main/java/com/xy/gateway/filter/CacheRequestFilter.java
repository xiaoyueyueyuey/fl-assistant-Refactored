package com.xy.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * 获取body请求数据（解决流不能重复读取问题）
 * Gateway filter for caching request body to avoid stream read issue\
 * 当请求到达时，首先检查请求的 HTTP 方法。如果是 GET 或 DELETE 方法
 * ，则直接传递请求到下一个过滤器，不进行处理。
 * 对于其他 HTTP 方法（POST、PUT 等），
 * 调用 ServerWebExchangeUtils.cacheRequestBodyAndRequest 方法
 * ，该方法会缓存请求体并替换原始请求，以确保后续的处理链能够重新读取请求体，
 * 避免流只能读取一次的问题。
 */
@Component
public class CacheRequestFilter extends AbstractGatewayFilterFactory<CacheRequestFilter.Config>
{
    public CacheRequestFilter()
    {
        super(Config.class);
    }

    @Override
    public String name()
    {
        return "CacheRequestFilter";
    }

    @Override
    public GatewayFilter apply(Config config)
    {
        CacheRequestGatewayFilter cacheRequestGatewayFilter = new CacheRequestGatewayFilter();// 创建GatewayFilter实现类
        Integer order = config.getOrder();
        if (order == null)
        {
            return cacheRequestGatewayFilter;// 如果未配置order，则直接返回GatewayFilter实现类
        }
        return new OrderedGatewayFilter(cacheRequestGatewayFilter, order);
    }

    /**
     * GatewayFilter实现类，用于处理请求
     * GatewayFilter implementation to process requests
     */
    public static class CacheRequestGatewayFilter implements GatewayFilter
    {
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
        {
            HttpMethod method = exchange.getRequest().getMethod();
            // GET和DELETE请求不过滤
            if (method == null || method == HttpMethod.GET || method == HttpMethod.DELETE)
            {
                return chain.filter(exchange); // 直接传递请求到下一个过滤器
            }

            // 缓存请求体并替换原始请求
            return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, serverHttpRequest -> {
                if (serverHttpRequest == exchange.getRequest())
                {
                    return chain.filter(exchange); // 请求未改变，直接传递到下一个过滤器
                }
                return chain.filter(exchange.mutate().request(serverHttpRequest).build()); // 使用新的请求对象传递到下一个过滤器
            });
        }
    }

    @Override
    public List<String> shortcutFieldOrder()
    {
        return Collections.singletonList("order");
    }

    /**
     * 内部配置类，用于接收过滤器配置参数
     * Configuration class to hold filter configuration parameters
     */
    static class Config
    {
        private Integer order;

        public Integer getOrder()
        {
            return order;
        }

        public void setOrder(Integer order)
        {
            this.order = order;
        }
    }
}
