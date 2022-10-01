package cloud.xuxiaowei.passport.service.impl;

import cloud.xuxiaowei.core.properties.CloudSecurityProperties;
import cloud.xuxiaowei.system.entity.Authorities;
import cloud.xuxiaowei.system.entity.Users;
import cloud.xuxiaowei.system.entity.WxOpenWebsiteUsers;
import cloud.xuxiaowei.system.service.IWxOpenWebsiteUsersService;
import cloud.xuxiaowei.utils.CodeEnums;
import cloud.xuxiaowei.utils.ResponseUtils;
import cloud.xuxiaowei.utils.exception.CloudRuntimeException;
import cloud.xuxiaowei.utils.exception.login.LoginException;
import cloud.xuxiaowei.utils.exception.oauth2.LoginAuthenticationException;
import cloud.xuxiaowei.utils.map.ResponseMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.WeChatOplatformWebsiteAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.client.InMemoryWeChatOplatformWebsiteService;
import org.springframework.security.oauth2.server.authorization.client.WeChatOplatformWebsiteService;
import org.springframework.security.oauth2.server.authorization.client.WeChatOplatformWebsiteTokenResponse;
import org.springframework.security.oauth2.server.authorization.exception.RedirectWeChatOplatformException;
import org.springframework.security.oauth2.server.authorization.properties.WeChatOplatformWebsiteProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 微信开放平台 网站应用 服务接口 实现类
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@Service
public class WeChatOplatformWebsiteServiceImpl implements WeChatOplatformWebsiteService {

	private WeChatOplatformWebsiteProperties weChatOplatformWebsiteProperties;

	private CloudSecurityProperties cloudSecurityProperties;

	private IWxOpenWebsiteUsersService wxOpenWebsiteUsersService;

	@Autowired
	public void setWeChatOplatformWebsiteProperties(WeChatOplatformWebsiteProperties weChatOplatformWebsiteProperties) {
		this.weChatOplatformWebsiteProperties = weChatOplatformWebsiteProperties;
	}

	@Autowired
	public void setCloudSecurityProperties(CloudSecurityProperties cloudSecurityProperties) {
		this.cloudSecurityProperties = cloudSecurityProperties;
	}

	@Autowired
	public void setWxOpenWebsiteUsersService(IWxOpenWebsiteUsersService wxOpenWebsiteUsersService) {
		this.wxOpenWebsiteUsersService = wxOpenWebsiteUsersService;
	}

	/**
	 * 构建 微信开放平台 网站应用 认证信息
	 * @param clientPrincipal 经过身份验证的客户端主体
	 * @param additionalParameters 附加参数
	 * @param details 登录信息
	 * @param appid AppID
	 * @param code 授权码
	 * @param openid 用户唯一标识
	 * @param credentials 证书
	 * @param unionid 多账户用户唯一标识
	 * @param accessToken 授权凭证
	 * @param refreshToken 刷新凭证
	 * @param expiresIn 过期时间
	 * @param scope 授权范围
	 * @return 返回 认证信息
	 * @throws OAuth2AuthenticationException OAuth 2.1 可处理的异常
	 */
	@Override
	public AbstractAuthenticationToken authenticationToken(Authentication clientPrincipal,
			Map<String, Object> additionalParameters, Object details, String appid, String code, String openid,
			Object credentials, String unionid, String accessToken, String refreshToken, Integer expiresIn,
			String scope) throws OAuth2AuthenticationException {

		WxOpenWebsiteUsers wxOpenWebsiteUsers = wxOpenWebsiteUsersService.getByAppidAndOpenid(appid, openid);

		if (wxOpenWebsiteUsers == null) {
			OAuth2Error error = new OAuth2Error(CodeEnums.ERROR.code, "未查询到微信用户或已被删除", null);
			throw new LoginAuthenticationException(error);
		}

		Users users = wxOpenWebsiteUsers.getUsers();
		String username;
		if (users == null) {
			OAuth2Error error = new OAuth2Error(CodeEnums.ERROR.code, "未找到微信绑定的用户", null);
			throw new LoginAuthenticationException(error);
		}

		username = users.getUsername();

		List<GrantedAuthority> authorities = new ArrayList<>();

		List<Authorities> authoritiesList = users.getAuthoritiesList();

		boolean allowEmptyAuthorities = cloudSecurityProperties.isAllowEmptyAuthorities();
		if (!allowEmptyAuthorities && authoritiesList.size() == 0) {
			throw new LoginException(CodeEnums.A10011.code, CodeEnums.A10011.msg);
		}

		for (Authorities auth : authoritiesList) {
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority(auth.getAuthority());
			authorities.add(authority);
		}

		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
				weChatOplatformWebsiteProperties.getDefaultRole());
		authorities.add(authority);
		User user = new User(username, accessToken, authorities);

		UsernamePasswordAuthenticationToken principal = UsernamePasswordAuthenticationToken.authenticated(user, null,
				user.getAuthorities());

		WeChatOplatformWebsiteAuthenticationToken authenticationToken = new WeChatOplatformWebsiteAuthenticationToken(
				authorities, clientPrincipal, principal, user, additionalParameters, details, appid, code, openid);

		authenticationToken.setCredentials(credentials);
		authenticationToken.setUnionid(unionid);

		return authenticationToken;
	}

	/**
	 * 根据 AppID、code、accessTokenUrl 获取Token
	 * @param appid AppID
	 * @param code 授权码
	 * @param accessTokenUrl 通过 code 换取网页授权 access_token 的 URL
	 * @param remoteAddress 用户IP
	 * @param sessionId SessionID
	 * @return 返回 微信授权结果
	 * @throws OAuth2AuthenticationException OAuth 2.1 可处理的异常
	 */
	@SneakyThrows
	@Override
	public WeChatOplatformWebsiteTokenResponse getAccessTokenResponse(String appid, String code, String accessTokenUrl,
			String userinfoUrl, String remoteAddress, String sessionId) throws OAuth2AuthenticationException {
		InMemoryWeChatOplatformWebsiteService weChatOplatformWebsiteService = new InMemoryWeChatOplatformWebsiteService(
				weChatOplatformWebsiteProperties);
		WeChatOplatformWebsiteTokenResponse accessTokenResponse = weChatOplatformWebsiteService
				.getAccessTokenResponse(appid, code, accessTokenUrl, userinfoUrl, remoteAddress, sessionId);

		String openid = accessTokenResponse.getOpenid();
		Integer expiresIn = accessTokenResponse.getExpiresIn();
		LocalDateTime expires = LocalDateTime.now().plusSeconds(expiresIn);
		String[] privilege = accessTokenResponse.getPrivilege();
		ObjectMapper objectMapper = new ObjectMapper();
		String privilegeStr = objectMapper.writeValueAsString(privilege);

		WxOpenWebsiteUsers wxOpenWebsiteUsers = wxOpenWebsiteUsersService.getByAppidAndOpenid(appid, openid);
		if (wxOpenWebsiteUsers == null) {

			WxOpenWebsiteUsers websiteUsers = new WxOpenWebsiteUsers();

			BeanUtils.copyProperties(accessTokenResponse, websiteUsers);

			websiteUsers.setPrivilege(privilegeStr);
			websiteUsers.setExpires(expires);
			websiteUsers.setCreateIp(remoteAddress);

			wxOpenWebsiteUsersService.save(websiteUsers);
		}
		else {

			BeanUtils.copyProperties(accessTokenResponse, wxOpenWebsiteUsers);

			wxOpenWebsiteUsers.setPrivilege(privilegeStr);
			wxOpenWebsiteUsers.setExpires(expires);
			wxOpenWebsiteUsers.setUpdateIp(remoteAddress);
			wxOpenWebsiteUsersService.updateById(wxOpenWebsiteUsers);
		}

		return accessTokenResponse;
	}

	/**
	 * 授权成功重定向方法
	 * @param request 请求
	 * @param response 响应
	 * @param uriVariables 参数
	 * @param oauth2AccessTokenResponse OAuth2.1 授权 Token
	 * @param weChatOplatformWebsite 微信开放平台 网站应用 配置
	 * @throws OAuth2AuthenticationException OAuth 2.1 可处理的异常
	 */
	@Override
	public void sendRedirect(HttpServletRequest request, HttpServletResponse response, Map<String, String> uriVariables,
			OAuth2AccessTokenResponse oauth2AccessTokenResponse,
			WeChatOplatformWebsiteProperties.WeChatOplatformWebsite weChatOplatformWebsite)
			throws OAuth2AuthenticationException {
		OAuth2AccessToken oauth2AccessToken = oauth2AccessTokenResponse.getAccessToken();
		OAuth2RefreshToken oauth2RefreshToken = oauth2AccessTokenResponse.getRefreshToken();

		String successUrl = weChatOplatformWebsite.getSuccessUrl();
		String accessToken = oauth2AccessToken.getTokenValue();
		String refreshToken = "";
		if (oauth2RefreshToken != null) {
			refreshToken = oauth2RefreshToken.getTokenValue();
		}

		try {
			response.sendRedirect(String.format("%s?store=true&accessToken=%s&refreshToken=%s", successUrl, accessToken,
					refreshToken));
		}
		catch (IOException e) {
			OAuth2Error error = new OAuth2Error(CodeEnums.ERROR.code, "微信开放平台 网站应用重定向异常", null);
			throw new RedirectWeChatOplatformException(error);
		}
	}

	/**
	 * 根据 appid 获取 微信开放平台 网站应用属性配置
	 * @param appid 公众号ID
	 * @return 返回 微信开放平台 网站应用属性配置
	 * @throws OAuth2AuthenticationException OAuth 2.1 可处理的异常
	 */
	@Override
	public WeChatOplatformWebsiteProperties.WeChatOplatformWebsite getWeChatOplatformWebsiteByAppid(String appid)
			throws OAuth2AuthenticationException {
		InMemoryWeChatOplatformWebsiteService weChatOplatformWebsiteService = new InMemoryWeChatOplatformWebsiteService(
				weChatOplatformWebsiteProperties);
		return weChatOplatformWebsiteService.getWeChatOplatformWebsiteByAppid(appid);
	}

	/**
	 * 根据 appid 获取重定向的地址
	 * @param appid 开放平台 网站应用 ID
	 * @return 返回重定向的地址
	 * @throws OAuth2AuthenticationException OAuth 2.1 可处理的异常
	 */
	@Override
	public String getRedirectUriByAppid(String appid) throws OAuth2AuthenticationException {
		InMemoryWeChatOplatformWebsiteService weChatOplatformWebsiteService = new InMemoryWeChatOplatformWebsiteService(
				weChatOplatformWebsiteProperties);
		return weChatOplatformWebsiteService.getRedirectUriByAppid(appid);
	}

	/**
	 * 获取 OAuth 2.1 授权 Token（如果不想执行此方法后面的内容，可返回 null）
	 * @param request 请求
	 * @param response 响应
	 * @param tokenUrlPrefix 获取 Token URL 前缀
	 * @param tokenUrl Token URL
	 * @param uriVariables 参数
	 * @return 返回 OAuth 2.1 授权 Token
	 * @throws OAuth2AuthenticationException OAuth 2.1 可处理的异常
	 */
	@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
	@Override
	public OAuth2AccessTokenResponse getOAuth2AccessTokenResponse(HttpServletRequest request,
			HttpServletResponse response, String tokenUrlPrefix, String tokenUrl, Map<String, String> uriVariables)
			throws OAuth2AuthenticationException {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
		messageConverters.add(5, new OAuth2AccessTokenResponseHttpMessageConverter());
		String postForObject = restTemplate.postForObject(tokenUrlPrefix + tokenUrl, httpEntity, String.class,
				uriVariables);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ResponseMap responseMap;
		try {
			responseMap = objectMapper.readValue(postForObject, ResponseMap.class);
		}
		catch (JsonProcessingException e) {
			throw new CloudRuntimeException(e);
		}

		String code = responseMap.getCode();
		if (code == null) {
			// 不存在响应代码，代表响应的为 OAuth 2.1 授权 Token

			Map<String, Object> map;
			try {
				map = objectMapper.readValue(postForObject, new TypeReference<Map<String, Object>>() {
				});
			}
			catch (JsonProcessingException e) {
				throw new CloudRuntimeException(e);
			}

			DefaultMapOAuth2AccessTokenResponseConverter converter = new DefaultMapOAuth2AccessTokenResponseConverter();
			return converter.convert(map);

		}
		else {
			// 存在响应代码时，代表程序报错，直接响应错误内容

			try {
				ResponseUtils.response(response, responseMap);
				return null;
			}
			catch (IOException e) {
				throw new CloudRuntimeException(e);
			}
		}
	}

}
