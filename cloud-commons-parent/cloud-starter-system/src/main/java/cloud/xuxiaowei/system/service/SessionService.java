package cloud.xuxiaowei.system.service;

import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * {@link HttpSession} 服务接口
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
public interface SessionService {

    /**
     * 获取 Token
     *
     * @return 返回 Token
     */
    String getTokenValue();

    /**
     * 获取 授权Token对象
     *
     * @return 返回 授权Token对象
     */
    OAuth2AccessToken readAccessToken();

    /**
     * 获取 默认授权Token对象
     *
     * @return 返回 默认授权Token对象
     */
    DefaultOAuth2AccessToken defaultOauth2AccessToken();

    /**
     * 获取 刷新Token对象
     *
     * @return 返回 刷新Token对象
     */
    DefaultExpiringOAuth2RefreshToken defaultExpiringOauth2RefreshToken();

    /**
     * 获取 刷新Token
     *
     * @return 返回 刷新Token
     */
    String getRefreshToken();

    /**
     * 计算令牌的MD5值
     *
     * @param value 令牌
     * @return 返回 令牌的MD5值
     */
    String extractTokenKey(String value);

    /**
     * 获取 授权Token ID
     * <p>
     * 身份验证未成功时（或：未进行身份验证），将返回 {@link HttpSession#getId()}
     *
     * @return 返回 授权Token ID，身份验证未成功时（或：未进行身份验证），将返回 {@link HttpSession#getId()}
     */
    String tokenId();

    /**
     * 设置 Session（Redis） 中的值
     *
     * @param key   键
     * @param value 值
     */
    void setAttribute(@NonNull String key, Object value);

    /**
     * 设置 Session（Redis） 中的值（自定义过期时间，不会跟随用户使用系统更新）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    过期时间单位
     */
    void setAttr(@NonNull String key, @NonNull String value, long timeout, @NonNull TimeUnit unit);

    /**
     * 设置 Redis 中的值（自定义过期时间，不会跟随用户使用系统更新）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    过期时间单位
     */
    void set(@NonNull String key, @NonNull String value, long timeout, @NonNull TimeUnit unit);

    /**
     * 获取 Session（Redis） 中的值
     *
     * @param key 键
     * @return 返回 值
     */
    Object getAttribute(@NonNull String key);

    /**
     * 移除 Session（Redis） 中的值
     *
     * @param key 键
     */
    void removeAttribute(@NonNull String key);

    /**
     * 移除 Session（Redis）
     *
     * @param key 键
     */
    void remove(@NonNull String key);

}
