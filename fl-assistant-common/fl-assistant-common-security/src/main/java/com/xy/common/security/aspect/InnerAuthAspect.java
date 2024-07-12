package com.xy.common.security.aspect;

import com.xy.common.core.constant.SecurityConstants;
import com.xy.common.core.exception.InnerAuthException;
import com.xy.common.core.utils.ServletUtils;
import com.xy.common.core.utils.StringUtils;
import com.xy.common.security.annotation.InnerAuth;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 内部服务调用验证处理
 * Aspect注解表示这是一个切面类，用于处理内部服务调用验证逻辑
 * Component注解表示这是一个Spring管理的组件
 * Ordered接口确保该切面在权限认证AOP执行前执行
 *
 * @author ruoyi
 */
@Aspect
@Component
public class InnerAuthAspect implements Ordered {

    /**
     * 在方法执行前后进行切面增强，处理@InnerAuth注解的方法
     *
     * @param point     切点，可以获取方法和参数信息
     * @param innerAuth InnerAuth注解，用于指定内部验证需求
     * @return 目标方法的返回值
     * @throws Throwable 抛出任何异常
     */
    @Around("@annotation(innerAuth)")
    public Object innerAround(ProceedingJoinPoint point, InnerAuth innerAuth) throws Throwable {
        // 获取请求的来源
        String source = ServletUtils.getRequest().getHeader(SecurityConstants.FROM_SOURCE);

        // 是不是内部请求，不是就抛异常
        if (!StringUtils.equals(SecurityConstants.INNER, source)) {
            throw new InnerAuthException("没有内部访问权限，不允许访问");
        }

        // 获取用户信息
        String userid = ServletUtils.getRequest().getHeader(SecurityConstants.DETAILS_USER_ID);
        String username = ServletUtils.getRequest().getHeader(SecurityConstants.DETAILS_USERNAME);

        // 用户信息验证
        if (innerAuth.isUser() && (StringUtils.isEmpty(userid) || StringUtils.isEmpty(username))) {
            throw new InnerAuthException("没有设置用户信息，不允许访问");
        }

        // 执行目标方法
        return point.proceed();
    }

    /**
     * 确保在权限认证AOP执行前执行
     * @return 返回优先级，确保在权限认证AOP之前执行
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
