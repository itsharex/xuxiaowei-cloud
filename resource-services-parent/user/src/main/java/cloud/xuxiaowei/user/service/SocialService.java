package cloud.xuxiaowei.user.service;

import cloud.xuxiaowei.user.vo.SocialVo;

import java.util.List;

/**
 * 社交 服务
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
public interface SocialService {

	/**
	 * 根据用户主键获取社交绑定
	 * @param usersId 用户主键
	 * @return 返回 社交绑定
	 */
	List<SocialVo> listByUsersId(Long usersId);

}
