package cloud.xuxiaowei.passportui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 授权码
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@Controller
@RequestMapping("/code")
public class CodeController {

    /**
     * 授权码页
     *
     * @param request  请求
     * @param response 响应
     * @param session  Session，不存在时自动创建
     * @return 返回 授权码页
     */
    @RequestMapping
    public String index(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        return "forward:index.html";
    }

}