//package com.ecommerce.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.*;
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
//        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(om, Object.class);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(serializer);
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(serializer);
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
//        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(10))
//                .serializeKeysWith(RedisSerializationContext.SerializationPair
//                        .fromSerializer(new StringRedisSerializer()));
//
//        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
//        configs.put("products", defaultConfig.entryTtl(Duration.ofMinutes(15)));
//        configs.put("categories", defaultConfig.entryTtl(Duration.ofHours(1)));
//        configs.put("banners", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        configs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(5)));
//
//        return RedisCacheManager.builder(factory)
//                .cacheDefaults(defaultConfig)
//                .withInitialCacheConfigurations(configs)
//                .build();
//    }
//}
