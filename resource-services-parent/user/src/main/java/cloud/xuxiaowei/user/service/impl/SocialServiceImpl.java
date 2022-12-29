package cloud.xuxiaowei.user.service.impl;

import cloud.xuxiaowei.system.mapper.SocialMapper;
import cloud.xuxiaowei.system.service.IGiteeUsersService;
import cloud.xuxiaowei.system.service.IQqWebsiteUsersService;
import cloud.xuxiaowei.system.service.IWeiBoWebsiteUsersService;
import cloud.xuxiaowei.system.service.IWxOpenWebsiteUsersService;
import cloud.xuxiaowei.system.vo.SocialVo;
import cloud.xuxiaowei.user.service.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 社交 服务
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@Service
public class SocialServiceImpl implements SocialService {

	@Resource
	private SocialMapper socialMapper;

	private IWxOpenWebsiteUsersService wxOpenWebsiteUsersService;

	private IGiteeUsersService giteeUsersService;

	private IQqWebsiteUsersService qqWebsiteUsersService;

	private IWeiBoWebsiteUsersService weiBoWebsiteUsersService;

	@Autowired
	public void setWxOpenWebsiteUsersService(IWxOpenWebsiteUsersService wxOpenWebsiteUsersService) {
		this.wxOpenWebsiteUsersService = wxOpenWebsiteUsersService;
	}

	@Autowired
	public void setGiteeUsersService(IGiteeUsersService giteeUsersService) {
		this.giteeUsersService = giteeUsersService;
	}

	@Autowired
	public void setQqWebsiteUsersService(IQqWebsiteUsersService qqWebsiteUsersService) {
		this.qqWebsiteUsersService = qqWebsiteUsersService;
	}

	@Autowired
	public void setWeiBoWebsiteUsersService(IWeiBoWebsiteUsersService weiBoWebsiteUsersService) {
		this.weiBoWebsiteUsersService = weiBoWebsiteUsersService;
	}

	/**
	 * 根据用户主键获取社交绑定
	 * @param usersId 用户主键
	 * @return 返回 社交绑定
	 */
	@Override
	public List<SocialVo> listByUsersId(Long usersId) {
		return socialMapper.listByUsersId(usersId);
	}

	/**
	 * 社交解绑
	 * @param usersId 用户主键
	 * @param socialCode 社交类型，1：微信扫码，2：码云Gitee
	 * @return 返回 解绑结果
	 */
	@Override
	public boolean unbinding(@NonNull Long usersId, @NonNull String socialCode) {
		switch (socialCode) {
			case "1":
				return wxOpenWebsiteUsersService.unbinding(usersId);
			case "2":
				return giteeUsersService.unbinding(usersId);
			case "3":
				return qqWebsiteUsersService.unbinding(usersId);
			case "4":
				return weiBoWebsiteUsersService.unbinding(usersId);
			default:
				return false;
		}
	}

}