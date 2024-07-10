package com.xy.gateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS跨站脚本配置
 * 
 * @author ruoyi
 */
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.xss")
public class XssProperties
{
    /**
     * Xss开关
     */
    private Boolean enabled; //与nacos配置中心的配置项对应

    /**
     * 排除路径
     */
    private List<String> excludeUrls = new ArrayList<>(); //与nacos配置中心的配置项对应

    public Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public List<String> getExcludeUrls()
    {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls)
    {
        this.excludeUrls = excludeUrls;
    }
}
