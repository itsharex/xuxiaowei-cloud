package cloud.xuxiaowei.gateway.filter;

import cloud.xuxiaowei.core.properties.CloudAesProperties;
import cloud.xuxiaowei.utils.Constant;
import cloud.xuxiaowei.utils.ResponseEncrypt;
import cn.hutool.crypto.symmetric.AES;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.List;

/**
 * 响应 Body 加密 过滤器
 *
 * @author xuxiaowei
 * @see ServerHttpResponseDecorator
 * @since 0.0.1
 */
@Slf4j
@Component
public class BodyEncryptionGlobalFilter implements GlobalFilter, Ordered {

	/**
	 * 最低优先级（最大值）：0
	 * <p>
	 * 大于 0 无效
	 */
	public static final int ORDERED = Ordered.HIGHEST_PRECEDENCE + 1010000;

	private CloudAesProperties cloudAesProperties;

	@Autowired
	public void setCloudAesProperties(CloudAesProperties cloudAesProperties) {
		this.cloudAesProperties = cloudAesProperties;
	}

	@Setter
	private int order = ORDERED;

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(exchange.getResponse()) {

			@NonNull
			@Override
			public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {

				HttpHeaders headers = exchange.getResponse().getHeaders();
				MediaType contentType = headers.getContentType();

				if (MediaType.APPLICATION_JSON.includes(contentType)) {
					// 响应数据为JSON，可以加密

					// 默认秘钥
					byte[] keyBytes = cloudAesProperties.getDefaultKey().getBytes();
					// 默认偏移量
					byte[] ivBytes = cloudAesProperties.getDefaultIv().getBytes();

					// 响应中的客户ID
					String clientId = headers.getFirst(OAuth2TokenIntrospectionClaimNames.CLIENT_ID);
					if (StringUtils.hasText(clientId)) {
						// 客户ID存在

						List<CloudAesProperties.Aes> aesList = cloudAesProperties.getList();
						// 遍历客户AES配置
						for (CloudAesProperties.Aes aesProperties : aesList) {
							if (clientId.equals(aesProperties.getClientId())) {
								// 匹配到客户的秘钥配置
								// 使用客户的秘钥配置
								keyBytes = aesProperties.getKey().getBytes();
								ivBytes = aesProperties.getIv().getBytes();
							}
						}
					}

					// 接口响应中的加密方式（版本）
					String encrypt = headers.getFirst(Constant.ENCRYPT);

					ServerHttpResponse response = getDelegate();

					if (StringUtils.hasText(encrypt)) {
						// 存在：响应中的加密方式（版本）

						// 匹配枚举
						ResponseEncrypt.AesVersion version = ResponseEncrypt.AesVersion.version(encrypt);
						if (version == null) {
							// 未匹配到枚举，使用默认加密方式（版本），即：V1
							return v1(exchange, response, keyBytes, ivBytes, body);
						}
						else {
							switch (version) {
							case V0:
								// 加密方式（版本）为 V0 时，即：不加密
								return exchange.getResponse().writeWith(body);
							case V1:
								// 加密方式（版本）为 V1 时，使用 V1，与未匹配时，采用相同的方式
								// 故：此处使用 switch case 的穿透效果
							default:
								// 未匹配到时，使用加密方式（版本）为 V1
								return v1(exchange, response, keyBytes, ivBytes, body);
							}
						}
					}
					else {
						// 不存在：响应中的加密方式（版本），使用默认加密方式（版本），即：V1
						return v1(exchange, response, keyBytes, ivBytes, body);
					}
				}

				// 响应数据不是JSON，不进行加密，直接返回数据
				return exchange.getResponse().writeWith(body);
			}

		};

		return chain.filter(exchange.mutate().response(decorator).build());
	}

	/**
	 * 加密方式（版本）V1
	 * @param exchange 服务器网络交换
	 * @param response 响应
	 * @param keyBytes 秘钥
	 * @param ivBytes 偏移量
	 * @param body 响应
	 * @return 返回加密后的数据
	 */
	private Mono<Void> v1(ServerWebExchange exchange, ServerHttpResponse response, byte[] keyBytes, byte[] ivBytes,
			Publisher<? extends DataBuffer> body) {

		HttpHeaders headers = exchange.getResponse().getHeaders();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		ResponseEncrypt.AesVersion aesVersion = ResponseEncrypt.AesVersion.V1;

		AES aes = new AES(aesVersion.mode, aesVersion.padding, keyBytes, ivBytes);

		// 设置加密版本
		headers.set(Constant.ENCRYPT, aesVersion.version);
		// 暴露响应头（否则 axios 将无法获取）
		headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, Constant.ENCRYPT);

		@SuppressWarnings("unchecked")
		Flux<? extends DataBuffer> fluxDataBuffer = (Flux<? extends DataBuffer>) body;

		return response.writeWith(fluxDataBuffer.buffer().map(dataBuffer -> {

			DataBuffer join = exchange.getResponse().bufferFactory().join(dataBuffer);

			byte[] bytes = new byte[join.readableByteCount()];
			join.read(bytes);
			DataBufferUtils.release(join);

			String originalText = new String(bytes);

			log.debug("加密前 body：{}", originalText);

			String encryptBase64 = aes.encryptBase64(originalText);

			log.debug("加密后 body：{}", encryptBase64);

			ResponseEncrypt responseEncrypt = new ResponseEncrypt();
			responseEncrypt.setCiphertext(encryptBase64);

			byte[] responseBytes;
			try {
				String value = objectMapper.writeValueAsString(responseEncrypt);

				log.debug("返回 body：{}", value);

				// 加密后的响应，设置响应内容的长度
				exchange.getResponse().getHeaders().setContentLength(value.length());

				responseBytes = value.getBytes();
			}
			catch (JsonProcessingException e) {
				log.error("body 加密后组装的对象转 JSON String 失败", e);
				throw new RuntimeException(e);
			}

			return exchange.getResponse().bufferFactory().wrap(responseBytes);
		}));
	}

}
