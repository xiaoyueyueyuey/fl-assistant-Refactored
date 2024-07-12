package com.xy.thirdparty.controller;

import com.xy.common.core.web.domain.AjaxResult;
import com.xy.common.security.annotation.InnerAuth;
import com.xy.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用
     *
     * @param phone
     * @param code
     * @return
     */
    @InnerAuth
    @GetMapping("/sendcode")
    public AjaxResult sendCode(@RequestParam("phone") String phone,
                               @RequestParam("code") String code) {
        System.out.println("......" + phone + ".." + code);
        smsComponent.sendSmsCode(phone, code);
        return AjaxResult.success();
    }
}
