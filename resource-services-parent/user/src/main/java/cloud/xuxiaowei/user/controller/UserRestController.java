package cloud.xuxiaowei.user.controller;

import cloud.xuxiaowei.system.bo.ManageUsersPageBo;
import cloud.xuxiaowei.system.service.IUsersService;
import cloud.xuxiaowei.system.vo.UsersVo;
import cloud.xuxiaowei.utils.AssertUtils;
import cloud.xuxiaowei.utils.Response;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

/**
 * 用户
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@RestController
public class UserRestController {

    private IUsersService usersService;

    @Autowired
    public void setUsersService(IUsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * 用户信息
     *
     * @param request  请求
     * @param response 响应
     * @return 返回 结果
     */
    @PreAuthorize("hasAuthority('user_info') or #oauth2.hasScope('user_info')")
    @RequestMapping("/info")
    public Response<?> info(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String name = authentication.getName();
        UsersVo usersVo = usersService.getUsersVoByUsername(name);
        if (usersVo == null) {
            return Response.error();
        }

        return Response.ok(usersVo);
    }

    /**
     * 用户权限
     *
     * @param request  请求
     * @param response 响应
     * @return 返回 结果
     */
    @PreAuthorize("hasAuthority('user_authorities') or #oauth2.hasScope('user_authorities')")
    @RequestMapping("/authorities")
    public Response<?> authorities(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        return Response.ok(authorities);
    }

    /**
     * 用户详情
     *
     * @param request  请求
     * @param response 响应
     * @return 返回 结果
     */
    @PreAuthorize("hasAuthority('user_details') or #oauth2.hasScope('user_details')")
    @RequestMapping("/details")
    public Response<?> details(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        Object details = authentication.getDetails();

        return Response.ok(details);
    }

    /**
     * oauth2 用户身份验证
     *
     * @param request  请求
     * @param response 响应
     * @return 返回 结果
     */
    @PreAuthorize("hasAuthority('user_oauth2_userAuthentication') or #oauth2.hasScope('user_oauth2_userAuthentication')")
    @RequestMapping("/userAuthentication")
    public Response<?> userAuthentication(HttpServletRequest request, HttpServletResponse response, OAuth2Authentication oauth2Authentication) {

        Authentication userAuthentication = oauth2Authentication.getUserAuthentication();

        return Response.ok(userAuthentication);
    }

    /**
     * oauth2 用户请求
     *
     * @param request  请求
     * @param response 响应
     * @return 返回 结果
     */
    @PreAuthorize("hasAuthority('user_oauth2_oauth2Request') or #oauth2.hasScope('user_oauth2_oauth2Request')")
    @RequestMapping("/oauth2Request")
    public Response<?> oauth2Request(HttpServletRequest request, HttpServletResponse response, OAuth2Authentication oauth2Authentication) {

        OAuth2Request oauth2Request = oauth2Authentication.getOAuth2Request();

        return Response.ok(oauth2Request);
    }

    /**
     * 分页查询用户
     *
     * @param request           请求
     * @param response          响应
     * @param manageUsersPageBo 用户分页参数
     * @return 返回 分页查询结果
     */
    @PreAuthorize("hasAuthority('manage_user_read') or #oauth2.hasScope('manage_user_read')")
    @RequestMapping("/page")
    public Response<?> page(HttpServletRequest request, HttpServletResponse response, @RequestBody ManageUsersPageBo manageUsersPageBo) {

        IPage<UsersVo> page = usersService.pageByManageUsers(manageUsersPageBo);

        return Response.ok(page);
    }

    /**
     * 根据 用户主键 删除
     *
     * @param request  请求
     * @param response 响应
     * @param usersId  用户主键
     * @return 返回 删除结果
     */
    @PreAuthorize("hasAuthority('manage_user_delete') or #oauth2.hasScope('manage_user_delete')")
    @RequestMapping("/removeById/{usersId}")
    public Response<?> removeById(HttpServletRequest request, HttpServletResponse response, @PathVariable("usersId") Long usersId) {

        boolean removeById = usersService.removeById(usersId);

        return Response.ok(removeById);
    }

    /**
     * 根据 用户主键 删除
     *
     * @param request  请求
     * @param response 响应
     * @param usersIds 用户主键
     * @return 返回 删除结果
     */
    @PreAuthorize("hasAuthority('manage_user_delete') or #oauth2.hasScope('manage_user_delete')")
    @RequestMapping("/removeByIds")
    public Response<?> removeByIds(HttpServletRequest request, HttpServletResponse response, @RequestBody List<Long> usersIds) {

        AssertUtils.sizeNonNull(usersIds, 1, 50, "非法数据长度");

        boolean removeByIds = usersService.removeByIds(usersIds);

        return Response.ok(removeByIds);
    }

}
