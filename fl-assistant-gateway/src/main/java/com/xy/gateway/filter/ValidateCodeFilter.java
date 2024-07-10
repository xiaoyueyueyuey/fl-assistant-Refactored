package com.xy.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xy.common.core.utils.ServletUtils;
import com.xy.common.core.utils.StringUtils;
import com.xy.gateway.config.properties.CaptchaProperties;
import com.xy.gateway.service.ValidateCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证码过滤器
 * Captcha filter for handling captcha validation
 */
@Component
public class ValidateCodeFilter extends AbstractGatewayFilterFactory<Object>
{
    private final static String[] VALIDATE_URL = new String[]{"/auth/login", "/auth/register"}; // 需要进行验证码校验的URL
    private static final String CODE = "code"; // 请求中验证码参数的键名
    private static final String UUID = "uuid"; // 请求中验证码UUID参数的键名
    @Autowired
    private ValidateCodeService validateCodeService; // 验证码服务
    @Autowired
    private CaptchaProperties captchaProperties; // 验证码配置属性

    @Override
    public GatewayFilter apply(Object config)
    {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 非登录/注册请求或验证码关闭，不处理
            if (!StringUtils.equalsAnyIgnoreCase(request.getURI().getPath(), VALIDATE_URL) || !captchaProperties.getEnabled())
            {
                return chain.filter(exchange); // 如果不是登录/注册请求或验证码未启用，则直接放行
            }

            try
            {
                String requestBody = resolveBodyFromRequest(request); // 解析请求体
                JSONObject obj = JSON.parseObject(requestBody); // 解析请求体为JSON对象
                validateCodeService.checkCaptcha(obj.getString(CODE), obj.getString(UUID)); // 校验验证码
            }
            catch (Exception e)
            {
                return ServletUtils.webFluxResponseWriter(exchange.getResponse(), e.getMessage()); // 发生异常时返回异常信息
            }

            return chain.filter(exchange); // 验证通过，继续执行过滤链
        };
    }

    /**
     * 从ServerHttpRequest中解析请求体内容
     * Resolve request body from ServerHttpRequest
     *
     * @param serverHttpRequest 请求对象
     * @return 请求体内容
     */
    private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest)
    {
        Flux<DataBuffer> body = serverHttpRequest.getBody(); // 获取请求体流
        AtomicReference<String> bodyRef = new AtomicReference<>(); // 原子引用，用于保存请求体字符串
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer()); // 解码请求体
            DataBufferUtils.release(buffer); // 释放缓冲区
            bodyRef.set(charBuffer.toString()); // 将解码后的字符串保存到原子引用中
        });
        return bodyRef.get(); // 返回请求体字符串
    }
}
