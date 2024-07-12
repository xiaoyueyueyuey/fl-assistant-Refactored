package com.xy.thirdparty.controller;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.xy.common.core.web.domain.AjaxResult;
import com.xy.common.log.annotation.Log;
import com.xy.common.log.enums.BusinessType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 这里使用的是springcloudalibaba oss ，集成了阿里云的oss
 */
@RequestMapping("third-party")
@RestController()
public class OssController {
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    String endpoint;
    // 填写Bucket名称，例如examplebucket。
    @Value("${spring.cloud.alicloud.oss.bucket}")
    String bucketName;
    @Value("${spring.cloud.alicloud.secret-key}")
    String accessKeySecret;
    @Value("${spring.cloud.alicloud.access-key}")
    String accessKeyId;

    /**
     * 前端获取oss的签名信息
     *
     * @return
     */
    @Log(title = "获取oss签名信息", businessType = BusinessType.GRANT)
    @GetMapping("/oss/policy")
    public AjaxResult policy() {

        //用来给前端返回的数据的oss地址
        String host = "https://" + bucketName + "." + endpoint;
        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 用户上传文件时指定的前缀，将当天上传的存在当天的文件夹中。
        String dir = format + "/";
        Map<String, String> respMap = null;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);


        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);


            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<>();
            respMap.put("accessid", accessKeyId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return AjaxResult.success("data", respMap);
    }

}
