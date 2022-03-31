package cloud.xuxiaowei.gateway.filter;

import cloud.xuxiaowei.utils.CodeEnums;
import cloud.xuxiaowei.utils.Response;
import cloud.xuxiaowei.utils.ResponseUtils;
import cloud.xuxiaowei.utils.ServiceConstant;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 监控（管理）服务 过滤器
 * <p>
 * 禁止通过网关访问 监控（管理）服务
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@Component
public class AdminServerGlobalFilter implements GlobalFilter, Ordered {

    public static final int ORDERED = Ordered.HIGHEST_PRECEDENCE + 20000;

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
        if (path.contains(ServiceConstant.ADMIN_SERVER)) {
            ServerHttpResponse response = exchange.getResponse();
            Response<?> error = Response.error(CodeEnums.X10002.code, CodeEnums.X10002.msg);
            return ResponseUtils.writeWith(response, error);
        }

        return chain.filter(exchange);
    }

}