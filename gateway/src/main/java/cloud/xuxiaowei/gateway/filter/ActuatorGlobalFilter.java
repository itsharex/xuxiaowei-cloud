package cloud.xuxiaowei.gateway.filter;

import cloud.xuxiaowei.utils.CodeEnums;
import cloud.xuxiaowei.utils.Response;
import cloud.xuxiaowei.utils.ResponseUtils;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 端点 过滤器
 * <p>
 * 禁止通过网关访问 服务的端点
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@Component
public class ActuatorGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 最低优先级（最大值）：0
     * <p>
     * 大于 0 无效
     */
    public static final int ORDERED = Ordered.HIGHEST_PRECEDENCE + 10000;

    @Setter
    private int order = ORDERED;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String path = uri.getPath();

        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean matchActuator = antPathMatcher.match("/*/actuator/**", path);

        if (matchActuator) {
            ServerHttpResponse response = exchange.getResponse();
            Response<?> error = Response.error(CodeEnums.X10001.code,CodeEnums.X10001.msg);
            return ResponseUtils.writeWith(response, error);
        }

        return chain.filter(exchange);
    }

}
