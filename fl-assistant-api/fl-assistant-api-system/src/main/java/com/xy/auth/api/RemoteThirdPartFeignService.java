package com.xy.auth.api;

import com.xy.auth.api.factory.RemoteThirdPartFallbackFactory;
import com.xy.common.core.constant.SecurityConstants;
import com.xy.common.core.constant.ServiceNameConstants;
import com.xy.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 第三方服务
 *
 * @author Xyue
 */

@FeignClient(contextId = "remoteThirdPartFeignService", value = ServiceNameConstants.THIRD_PART_SERVICE, fallbackFactory = RemoteThirdPartFallbackFactory.class)
public interface RemoteThirdPartFeignService {

    @GetMapping("/sms/sendcode")
    R<Boolean> sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

}
