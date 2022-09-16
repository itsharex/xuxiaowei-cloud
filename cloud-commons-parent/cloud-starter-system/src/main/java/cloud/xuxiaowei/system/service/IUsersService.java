package cloud.xuxiaowei.system.service;

import cloud.xuxiaowei.system.bo.*;
import cloud.xuxiaowei.system.entity.Users;
import cloud.xuxiaowei.system.vo.ForgetVo;
import cloud.xuxiaowei.system.vo.UsersVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户表。
 * 原表结构：spring-security-core-*.*.*.jar!/org/springframework/security/core/userdetails/jdbc/users.ddl
 * 原表结构：https://github.com/spring-projects/spring-security/blob/main/core/src/main/resources/org/springframework/security/core/userdetails/jdbc/users.ddl
 * GitCode
 * 镜像仓库：https://gitcode.net/mirrors/spring-projects/spring-security/blob/main/core/src/main/resources/org/springframework/security/core/userdetails/jdbc/users.ddl
 * 服务类
 * </p>
 *
 * @author xuxiaowei
 * @since 2022-04-04
 */
public interface IUsersService extends IService<Users> {

	/**
	 * 按用户名加载用户及权限（包含用户组权限）
	 * <p>
	 * 权限为空已剔除
	 * @param username 用户名
	 * @return 返回 用户信息及权限（包含用户组权限）
	 */
	Users loadUserByUsername(String username);

	/**
	 * 根据 用户名 查询用户信息及权限
	 * @param username 用户名
	 * @return 返回 用户信息及权限
	 */
	Users getByUsername(String username);

	/**
	 * 根据 用户名 查询用户信息
	 * <p>
	 * 条件无逻辑删除的判断
	 * @param username 用户名
	 * @return 返回 用户信息
	 */
	Users getLogicByUsername(String username);

	/**
	 * 根据 昵称 查询用户信息
	 * @param nickname 昵称
	 * @return 返回 用户信息
	 */
	Users getByNickname(String nickname);

	/**
	 * 根据 昵称 查询用户信息
	 * <p>
	 * 条件无逻辑删除的判断
	 * @param nickname 昵称
	 * @return 返回 用户信息
	 */
	Users getLogicByNickname(String nickname);

	/**
	 * 根据 用户名 查询用户信息、性别、区域地址及权限
	 * @param username 用户名
	 * @return 返回 用户信息、性别、区域地址及权限
	 */
	UsersVo getUsersVoByUsername(String username);

	/**
	 * 分页查询用户
	 * @param manageUsersPageBo 管理用户分页参数
	 * @return 返回 分页查询结果
	 */
	IPage<UsersVo> pageByManageUsers(ManageUsersPageBo manageUsersPageBo);

	/**
	 * 根据 用户主键 查询
	 * @param usersId 用户主键
	 * @return 返回 查询结果
	 */
	UsersVo getUsersVoById(Long usersId);

	/**
	 * 保存用户
	 * @param usersSaveBo 用户
	 * @return 返回 保存结果
	 */
	boolean saveUsersSaveBo(UsersSaveBo usersSaveBo);

	/**
	 * 更新用户
	 * @param usersUpdateByIdBo 用户
	 * @return 返回 更新结果
	 */
	boolean updateByUsersUpdateByIdBo(UsersUpdateByIdBo usersUpdateByIdBo);

	/**
	 * 根据当前操作人更新用户
	 * @param usersUpdateBo 用户表
	 * @return 返回 更新结果
	 */
	boolean updateByUsersUpdateBo(UsersUpdateBo usersUpdateBo);

	/**
	 * 获取不是某个用户是否存在指定邮箱的用户
	 * @param usersId 用户ID
	 * @param email 邮箱
	 * @param deleted 是否逻辑删除
	 * @return 返回 用户信息
	 */
	List<Users> listByIdNotUsersIdAndEmail(Long usersId, String email, Boolean deleted);

	/**
	 * 获取不是某个用户是否存在指定昵称的用户
	 * @param usersId 用户ID
	 * @param nickname 昵称
	 * @param deleted 是否逻辑删除
	 * @return 返回 用户信息
	 */
	List<Users> listByIdNotUsersIdAndNickname(Long usersId, String nickname, Boolean deleted);

	/**
	 * 根据 邮箱 查询用户
	 * @param email 邮箱
	 * @return 返回 查询结果
	 */
	Users getByEmail(String email);

	/**
	 * 根据 邮箱 查询用户信息
	 * <p>
	 * 条件无逻辑删除的判断
	 * @param email 邮箱
	 * @return 返回 用户信息
	 */
	Users getLogicByEmail(String email);

	/**
	 * 根据 用户名/手机号码/绑定邮箱 查询绑定的 手机号码/绑定邮箱（已脱敏的，使用 Getter 进行脱敏）
	 * @param forgetBo 用户名/手机号码/绑定邮箱
	 * @return 返回 手机号码/绑定邮箱（已脱敏的，使用 Getter 进行脱敏）
	 */
	ForgetVo getForgetVoByForgetBo(ForgetBo forgetBo);

}
