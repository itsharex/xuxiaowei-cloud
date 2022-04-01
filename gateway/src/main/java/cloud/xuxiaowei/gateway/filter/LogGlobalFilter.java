package cloud.xuxiaowei.gateway.filter;

import cloud.xuxiaowei.log.entity.Log;
import cloud.xuxiaowei.log.service.ILogService;
import cloud.xuxiaowei.utils.CodeEnums;
import cloud.xuxiaowei.utils.Response;
import cloud.xuxiaowei.utils.ResponseUtils;
import cloud.xuxiaowei.utils.ServiceConstant;
import cloud.xuxiaowei.utils.reactive.RequestUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static cloud.xuxiaowei.utils.Constant.IP;

/**
 * 日志 过滤器
 *
 * @author xuxiaowei
 * @since 0.0.1
 */
@Slf4j
@Component
public class LogGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 最低优先级（最大值）：0
     * <p>
     * 大于 0 无效
     */
    public static final int ORDERED = Ordered.HIGHEST_PRECEDENCE;

    private ILogService logService;

    @Autowired
    public void setLogService(ILogService logService) {
        this.logService = logService;
    }

    @Setter
    private int order = ORDERED;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String requestId = request.getId();
        MDC.put(Response.REQUEST_ID, requestId);

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null) {
            Response<?> error = Response.error(CodeEnums.X10003.code, CodeEnums.X10003.msg);
            return ResponseUtils.writeWith(response, error);
        }

        InetAddress address = remoteAddress.getAddress();
        String remoteHost = address.getHostAddress();
        MDC.put(IP, remoteHost);

        save(logService, request, remoteHost);

        return chain.filter(exchange);
    }

    /**
     * 根据请求保存数据
     *
     * @param logService 日志服务
     * @param request    请求
     * @param remoteHost 用户IP
     */
    public static void save(ILogService logService, ServerHttpRequest request, String remoteHost) {

        LocalDate localDate = LocalDate.now();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        LocalTime localTime = LocalTime.now();
        int hour = localTime.getHour();

        String method = request.getMethodValue();
        String requestId = request.getId();

        String headersMap = RequestUtils.getHeadersJson(request);
        String userAgent = RequestUtils.getUserAgent(request);

        URI uri = request.getURI();
        String requestUri = uri.getPath();
        String queryString = uri.getQuery();

        Log log = new Log();
        log.setModule(ServiceConstant.GATEWAY);
        log.setDate(localDate);
        log.setYear(year);
        log.setMonth(month);
        log.setDay(day);
        log.setHour(hour);
        log.setMethod(method);
        log.setRequestUri(requestUri);
        log.setQueryString(queryString);
        log.setHeadersMap(headersMap);
        log.setUserAgent(userAgent);
        log.setRequestId(requestId);
        log.setSessionId(null);
        log.setCreateUsername("该字段待确认");
        log.setCreateIp(remoteHost);
        log.setCreateDate(LocalDateTime.now());

        logService.save(log);
    }

}
