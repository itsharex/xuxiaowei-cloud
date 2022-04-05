package cloud.xuxiaowei.gateway.filter;

import cloud.xuxiaowei.core.properties.CloudCorsProperties;
import cloud.xuxiaowei.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static cloud.xuxiaowei.utils.Constant.NULL;

/**
 * 在 CORS 之前执行
 *
 * @author xuxiaowei
 * @see DefaultCorsProcessor#process(CorsConfiguration, ServerWebExchange)
 * @since 0.0.1
 */
@Component
public class CorsBeforeWebFilter implements WebFilter {

    private CloudCorsProperties cloudCorsProperties;

    @Autowired
    public void setCloudCorsProperties(CloudCorsProperties cloudCorsProperties) {
        this.cloudCorsProperties = cloudCorsProperties;
    }

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        URI uri = request.getURI();
        String path = uri.getPath();

        String origin = RequestUtils.getOrigin(request);

        if (origin == null || NULL.equals(origin)) {
            List<String> allowOrginNullList = cloudCorsProperties.getAllowOrginNullList();
            if (allowOrginNullList != null) {
                // 解决 form action 提交数据无 origin 跨域问题
                if (allowOrginNullList.contains(path)) {
                    String schemeHost = RequestUtils.getSchemeHost(request);
                    response.getHeaders().setAccessControlAllowOrigin(schemeHost);
                }
            }
        }

        return chain.filter(exchange);
    }

}
