package com.xy.auth.api.factory;

import com.xy.auth.api.RemoteThirdPartFeignService;
import com.xy.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 第三方服务降级处理
 */

@Component
public class RemoteThirdPartFallbackFactory implements FallbackFactory<RemoteThirdPartFeignService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteThirdPartFallbackFactory.class);

    @Override
    public RemoteThirdPartFeignService create(Throwable cause) {
        log.error("第三方服务调用失败:{}", cause.getMessage());
        return new RemoteThirdPartFeignService() {
            @Override
            public R<Boolean> sendCode(String phone, String code, String source) {
                return R.fail("调用失败:" + cause.getMessage());
            }
        };
    }
}
