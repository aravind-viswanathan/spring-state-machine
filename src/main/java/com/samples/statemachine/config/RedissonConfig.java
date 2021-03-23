package com.samples.statemachine.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import com.samples.statemachine.locks.RedissonCache;
import com.samples.statemachine.repository.HashMapPersister;
import com.sun.istack.Nullable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.LZ4Codec;
import org.redisson.config.Config;
import org.redisson.spring.cache.NullValue;
import org.redisson.spring.cache.RedissonCacheStatisticsAutoConfiguration;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

@Configuration
@EnableAutoConfiguration(exclude = {RedissonCacheStatisticsAutoConfiguration.class})
@EnableCaching
public class RedissonConfig {

    @Bean(destroyMethod="shutdown")
    RedissonClient redisson(@Value("classpath:/redisson.yaml") Resource configFile) throws IOException {
        Config config = Config.fromYAML(configFile.getInputStream());
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
                .registerModule((new SimpleModule()).addSerializer(new RedissonConfig.NullValueSerializer(null)));
        JsonJacksonCodec codec = new JsonJacksonCodec(mapper);
        config.setCodec(codec);
        return Redisson.create(config);
    }

    @Bean
    CacheManager cacheManager(RedissonClient redissonClient)  {
        return new RedissonSpringCacheManager(redissonClient);
    }


    @Bean
    public StateMachinePersist<States, Events, UUID> hashMapPersister(RedissonCache<UUID, StateMachineContext<States, Events>> cache){
        return new HashMapPersister(cache);
    }

    @Bean
    public StateMachinePersister<States, Events, UUID> persister(StateMachinePersist<States, Events, UUID> persist){
        return new DefaultStateMachinePersister<>(persist);
    }

    private static class NullValueSerializer extends StdSerializer<NullValue>{

        private final String classIdentifier;

        public NullValueSerializer(@Nullable String classIdentifier){
            super(NullValue.class);
            this.classIdentifier = StringUtils.hasText(classIdentifier)?classIdentifier:"@class";
        }

        @Override
        public void serialize(NullValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField(this.classIdentifier, NullValue.class.getName());
            gen.writeEndObject();
        }
    }

}
