package com.xy.auth.controller;

import com.xy.auth.api.RemoteThirdPartFeignService;
import com.xy.auth.form.LoginBody;
import com.xy.auth.form.RegisterBody;
import com.xy.auth.service.SysLoginService;
import com.xy.common.core.constant.CacheConstants;
import com.xy.common.core.constant.SecurityConstants;
import com.xy.common.core.domain.R;
import com.xy.common.core.utils.JwtUtils;
import com.xy.common.core.utils.StringUtils;
import com.xy.common.redis.service.RedisService;
import com.xy.common.security.auth.AuthUtil;
import com.xy.common.security.service.TokenService;
import com.xy.common.security.utils.SecurityUtils;
import com.xy.system.api.model.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * token 控制
 * 
 * @author ruoyi
 */
@RequiredArgsConstructor
@RestController
public class TokenController
{
    private final TokenService tokenService;

    private final SysLoginService sysLoginService;

    private final RedisService redisService;

    private final RemoteThirdPartFeignService thirdPartFeignService;

    //处理登录请求
    @PostMapping("login")
    public R<?> login(@RequestBody LoginBody form)
    {
        // 用户登录,这里只有账号密码登录，没有token一键登录，账号密码都是存在前端了，密码二次加密在前端，传过来解密了
        LoginUser userInfo = sysLoginService.login(form.getUsername(), form.getPassword());
        // 获取登录token
        return R.ok(tokenService.createToken(userInfo));
    }

    @DeleteMapping("logout")
    public R<?> logout(HttpServletRequest request)
    {
        String token = SecurityUtils.getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            String username = JwtUtils.getUserName(token);
            // 删除用户缓存记录
            AuthUtil.logoutByToken(token);
            // 记录用户退出日志
            sysLoginService.logout(username);
        }
        return R.ok();
    }
    @PostMapping("refresh")
    public R<?> refresh(HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            // 刷新令牌有效期
            tokenService.refreshToken(loginUser);
            return R.ok();
        }
        return R.ok();
    }

    @PostMapping("register")
    public R<?> register(@RequestBody RegisterBody registerBody)
    {
        // 用户注册
        sysLoginService.register(registerBody.getUsername(), registerBody.getPassword());
        return R.ok();
    }

    @GetMapping(value = "smscode")
    public R<?> sendSmsCode(@RequestParam("mobile") String mobile) {
        String redisCode = redisService.getCacheObject(CacheConstants.SMS_CODE_LOGIN_PREFIX + mobile);
        if (!com.baomidou.mybatisplus.core.toolkit.StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
//            没超过60S，不能发送
                return R.fail("验证码发送过于频繁，请稍后再试");
            }
        }
        //        接口防刷
//        UUID uuid = UUID.randomUUID();
//        String str = uuid.toString().replace("-", "");
//        String code = str.substring(0, 5);
//        生成五位随机数，上面的UUID生成的不一定是5位数字
        Random random = new Random();
        StringBuilder code1 = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int digit = random.nextInt(10);
            code1.append(digit);
        }
        String code = code1.toString();
//        给redis缓存code和phone的绑定关系，加currentTimeMillis是为了判断手机号发送时间是否超过了60S
        String codePlus = code + "_" + System.currentTimeMillis();
        redisService.setCacheObject(CacheConstants.SMS_CODE_LOGIN_PREFIX + mobile, codePlus, CacheConstants.SMS_EXPIRATION, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(mobile, code, SecurityConstants.INNER);//发送验证码,内部调用
        return R.ok();
    }

}
