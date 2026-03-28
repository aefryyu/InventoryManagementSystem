package ims.aefryyu.server.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public ProxyManager<String> proxyManager() {

        RedisClient redisClient = RedisClient.create("redis://localhost:6379");

        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(new io.lettuce.core.codec.RedisCodec<String, byte[]>() {

                    private final io.lettuce.core.codec.StringCodec keyCodec = io.lettuce.core.codec.StringCodec.UTF8;
                    private final io.lettuce.core.codec.ByteArrayCodec valueCodec = new io.lettuce.core.codec.ByteArrayCodec();

                    @Override
                    public String decodeKey(java.nio.ByteBuffer bytes) {
                        return keyCodec.decodeKey(bytes);
                    }

                    @Override
                    public byte[] decodeValue(java.nio.ByteBuffer bytes) {
                        return valueCodec.decodeValue(bytes);
                    }

                    @Override
                    public java.nio.ByteBuffer encodeKey(String key) {
                        return keyCodec.encodeKey(key);
                    }

                    @Override
                    public java.nio.ByteBuffer encodeValue(byte[] value) {
                        return valueCodec.encodeValue(value);
                    }
                });

        return LettuceBasedProxyManager.builderFor(connection).build();
    }
}
