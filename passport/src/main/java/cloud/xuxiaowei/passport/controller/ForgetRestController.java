package cloud.xuxiaowei.passport.controller;

import cloud.xuxiaowei.core.properties.CloudSecurityProperties;
import cloud.xuxiaowei.passport.bo.CheckResetPasswordTokenBo;
import cloud.xuxiaowei.passport.bo.ResetPasswordBo;
import cloud.xuxiaowei.system.annotation.ControllerAnnotation;
import cloud.xuxiaowei.system.bo.ForgetBo;
import cloud.xuxiaowei.system.entity.Users;
import cloud.xuxiaowei.system.service.IUsersService;
import cloud.xuxiaowei.system.service.SessionService;
import cloud.xuxiaowei.utils.DateUtils;
import cloud.xuxiaowei.utils.Response;
import cloud.xuxiaowei.utils.exception.CloudRuntimeException;
import cloud.xuxiaowei.utils.map.ResponseMap;
import cn.hutool.core.util.DesensitizedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cloud.xuxiaowei.utils.DateUtils.DEFAULT_DATE_TIME_FORMAT;

/**
 * 忘记密码
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@RestController
public class ForgetRestController {

	private SessionService sessionService;

	private IUsersService usersService;

	private JavaMailSender javaMailSender;

	private MailProperties mailProperties;

	private CloudSecurityProperties cloudSecurityProperties;

	@Autowired
	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@Autowired
	public void setUsersService(IUsersService usersService) {
		this.usersService = usersService;
	}

	/**
	 * 注意： 当未成功配置邮箱时，{@link Autowired} 直接注入将会失败，导致程序无法启动
	 * <p>
	 * 故将 {@link Autowired} 的 required 设置为 false，避免程序启动失败。使用时请判断该值是否为 null
	 */
	@Autowired(required = false)
	public void setJavaMailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	@Autowired
	public void setMailProperties(MailProperties mailProperties) {
		this.mailProperties = mailProperties;
	}

	@Autowired
	public void setCloudSecurityProperties(CloudSecurityProperties cloudSecurityProperties) {
		this.cloudSecurityProperties = cloudSecurityProperties;
	}

	/**
	 * 忘记密码
	 * @param request 请求
	 * @param response 响应
	 * @return 返回 结果
	 */
	@ControllerAnnotation(description = "忘记密码")
	@RequestMapping("/forget")
	public Response<?> forget(HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody ForgetBo forgetBo) {

		String username = forgetBo.getUsername();

		Users byUsername = usersService.getByUsername(username);
		if (byUsername != null) {
			String email = byUsername.getEmail();
			String phone = byUsername.getPhone();
			if (StringUtils.hasText(email)) {

				email(byUsername);

				return ResponseMap.ok(String.format("我们向邮箱 %s 发送了一封含有重置密码链接的邮件。请登录邮箱查看，如长时间没有收到邮件，请检查你的垃圾邮件文件夹。",
						DesensitizedUtil.email(email))).put("type", "email");
			}
			else if (StringUtils.hasText(phone)) {
				return ResponseMap
						.ok(String.format("一条包含验证码的信息已发送至你的 手机 %s，请输入验证码以继续", DesensitizedUtil.mobilePhone(phone)))
						.put("usersId", byUsername.getUsersId()).put("type", "phone");
			}
			else {
				return ResponseMap.error("账户未绑定手机号/邮箱");
			}
		}

		Users byEmail = usersService.getByEmail(username);
		if (byEmail != null) {
			String email = byEmail.getEmail();

			email(byEmail);

			return ResponseMap.ok(String.format("我们向邮箱 %s 发送了一封含有重置密码链接的邮件。请登录邮箱查看，如长时间没有收到邮件，请检查你的垃圾邮件文件夹。",
					DesensitizedUtil.email(email))).put("type", "email");
		}

		Users byPhone = usersService.getByPhone(username);
		if (byPhone != null) {
			String phone = byPhone.getPhone();
			return ResponseMap
					.ok(String.format("一条包含验证码的信息已发送至你的 手机 %s，请输入验证码以继续", DesensitizedUtil.mobilePhone(phone)))
					.put("usersId", byPhone.getUsersId()).put("type", "phone");
		}

		return Response.error("未找到用户");
	}

	private void email(Users user) {
		if (javaMailSender == null) {
			throw new CloudRuntimeException(String.format("错误：邮箱：%s 未登录，不可发送邮件！！！", mailProperties.getUsername()));
		}

		Long usersId = user.getUsersId();
		String username = user.getUsername();
		String email = user.getEmail();
		String nickname = user.getNickname();

		int hours = cloudSecurityProperties.getResetPasswordTokenHours();

		String token = UUID.randomUUID().toString();
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expire = now.plusHours(hours);

		sessionService.set("reset-password-token:" + usersId, token, hours, TimeUnit.HOURS);

		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setFrom(mailProperties.getUsername());
		simpleMailMessage.setTo(email);
		simpleMailMessage.setSubject("重置密码");

		// @formatter:off
        simpleMailMessage.setText(String.format("您好 %s (%s)！ \n\n" +
                        "您已经请求了重置密码，可以点击下面的链接来重置密码。 \n\n" +
                        "http://passport.example.xuxiaowei.cloud:1411/#/reset-password?usersId=%s&reset_password_token=%s \n\n" +
                        "如果您没有请求重置密码，请忽略这封邮件。 \n\n" +
                        "在您点击上面链接修改密码之前，您的密码将会保持不变。 \n\n" +
                        "链接有效期 %s 小时(%s 过期)",
                username, nickname, usersId, token, hours, DateUtils.format(expire, DEFAULT_DATE_TIME_FORMAT)));
        // @formatter:on

		javaMailSender.send(simpleMailMessage);
	}

	/**
	 * 检查重置密码凭证
	 * @param request 请求
	 * @param response 响应
	 * @return 返回 结果
	 */
	@ControllerAnnotation(description = "检查重置密码凭证")
	@RequestMapping("/check-reset-password-token")
	public Response<?> checkResetPasswordToken(HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody CheckResetPasswordTokenBo checkResetPasswordTokenBo) {

		Long usersId = checkResetPasswordTokenBo.getUsersId();
		String resetPasswordToken = checkResetPasswordTokenBo.getResetPasswordToken();
		String token = sessionService.get("reset-password-token:" + usersId);
		if (resetPasswordToken.equals(token)) {
			return Response.ok();
		}

		return Response.error("重置密码凭证已失效");
	}

	/**
	 * 重置密码
	 * @param request 请求
	 * @param response 响应
	 * @return 返回 结果
	 */
	@ControllerAnnotation(description = "重置密码")
	@RequestMapping("/reset-password")
	public Response<?> resetPassword(HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody ResetPasswordBo resetPasswordBo) {

		Long usersId = resetPasswordBo.getUsersId();
		String resetPasswordToken = resetPasswordBo.getResetPasswordToken();
		String password = resetPasswordBo.getPassword();

		HttpSession session = request.getSession();

		String rsaPrivateKeyBase64 = (String) session.getAttribute("RSA_PRIVATE_KEY_BASE64");

		String token = sessionService.get("reset-password-token:" + usersId);
		if (resetPasswordToken.equals(token)) {
			usersService.updatePasswordById(usersId, password, rsaPrivateKeyBase64);
			sessionService.remove("reset-password-token:" + usersId);
			return Response.ok();
		}

		return Response.error("重置密码凭证已失效");
	}

}
