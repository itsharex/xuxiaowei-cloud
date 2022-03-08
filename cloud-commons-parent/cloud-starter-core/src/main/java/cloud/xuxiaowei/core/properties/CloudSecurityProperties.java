package cloud.xuxiaowei.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微服务 Security 配置
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@Data
@Component
@ConfigurationProperties("cloud.security")
public class CloudSecurityProperties {

    /**
     * 登录页面地址
     */
    private String loginPageUrl = "http://passport.xuxiaowei.cloud";

}