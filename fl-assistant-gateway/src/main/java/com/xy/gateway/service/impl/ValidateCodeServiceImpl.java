package com.xy.gateway.service.impl;

import com.google.code.kaptcha.Producer;
import com.xy.common.core.constant.CacheConstants;
import com.xy.common.core.constant.Constants;
import com.xy.common.core.exception.CaptchaException;
import com.xy.common.core.utils.StringUtils;
import com.xy.common.core.utils.sign.Base64;
import com.xy.common.core.utils.uuid.IdUtils;
import com.xy.common.core.web.domain.AjaxResult;
import com.xy.common.redis.service.RedisService;
import com.xy.gateway.config.properties.CaptchaProperties;
import com.xy.gateway.service.ValidateCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码实现处理
 *
 * @author ruoyi
 */
@Service
public class ValidateCodeServiceImpl implements ValidateCodeService
{
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CaptchaProperties captchaProperties;

    /**
     * 生成验证码
     */
    @Override
    public AjaxResult createCaptcha() throws IOException, CaptchaException
    {
        AjaxResult ajax = AjaxResult.success();
        boolean captchaEnabled = captchaProperties.getEnabled();//查看验证码是否启用，启动再创建
        ajax.put("captchaEnabled", captchaEnabled);//将验证码是否启用的信息放入ajax结果中
        if (!captchaEnabled)//如果验证码未启用，直接返回
        {
            return ajax;
        }

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;// 验证码key，存入redis

        String capStr = null, code = null;
        BufferedImage image = null;

        String captchaType = captchaProperties.getType();
        // 生成验证码
        if ("math".equals(captchaType))
        {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        }
        else if ("char".equals(captchaType))
        {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        redisService.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);// 将验证码存入redis
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", os);
        }
        catch (IOException e)
        {
            return AjaxResult.error(e.getMessage());
        }

        ajax.put("uuid", uuid);
        ajax.put("img", Base64.encode(os.toByteArray()));
        return ajax;
    }

    /**
     * 校验验证码
     */
    @Override
    public void checkCaptcha(String code, String uuid) throws CaptchaException
    {
        if (StringUtils.isEmpty(code))
        {
            throw new CaptchaException("验证码不能为空");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");// 验证码key，从redis中获取
        String captcha = redisService.getCacheObject(verifyKey);// 从redis中获取验证码
        if (captcha == null)
        {
            throw new CaptchaException("验证码已失效");
        }
        redisService.deleteObject(verifyKey);// 删除验证码
        if (!code.equalsIgnoreCase(captcha))// 验证码校验
        {
            throw new CaptchaException("验证码错误");
        }
    }
}
