package com.jkh1447.MyProject.global.config.redis;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean
    public DefaultRedisScript<Long> incrementAcceptCountScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
            -- KEYS[1]: matchStatusKey
            -- ARGV[1]: userId

            if redis.call('EXISTS', KEYS[1]) == 0 then
                return -2;
            end

            local userField = "user:" .. ARGV[1]

            if redis.call('HEXISTS', KEYS[1], userField) == 1 then
                return -1;
            end

            local groupSize = redis.call('HGET', KEYS[1], 'groupSize')
            if not groupSize then
                return -2;
            end

            redis.call('HSET', KEYS[1], userField, '"ACCEPTED"')
            local currentCount = redis.call('HINCRBY', KEYS[1], 'acceptCount', 1)

            if currentCount == tonumber(groupSize) then
              redis.call('HSET', KEYS[1], 'status', '"COMPLETED"')
              return 1
            else
              return 0
            end
                """);
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean
    public DefaultRedisScript<Long> removeUserFromQueueScript() {
      DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
      redisScript.setScriptText("""
            redis.call('ZREM', KEYS[1], ARGV[1])
            local plainId = ARGV[1]
            if string.sub(ARGV[1], 1, 1) == '"' and string.sub(ARGV[1], -1) == '"' then
                plainId = string.sub(ARGV[1], 2, -2)
            end
            redis.call('HDEL', KEYS[2], plainId)
            redis.call('HDEL', KEYS[3], plainId)    
            return 1
            """);
      redisScript.setResultType(Long.class);
      return redisScript;
    }

    @Bean
    public DefaultRedisScript<Long> removeUsersFromQueueScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
            local count = 0
            for i, userId in ipairs(ARGV) do
                local removed = redis.call('ZREM', KEYS[1], userId)
                local plainId = userId
                if string.sub(userId, 1, 1) == '"' and string.sub(userId, -1) == '"' then
                    plainId = string.sub(userId, 2, -2)
                end
                redis.call('HDEL', KEYS[2], plainId)
                redis.call('HDEL', KEYS[3], plainId)
                if removed > 0 then count = count + 1 end
            end
            return count
        """);
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean
    public DefaultRedisScript<List> processMatchTimeoutScript() {
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("""
            if redis.call('HEXISTS', KEYS[1], 'status') == 1 then
                return nil
            end

            local data = redis.call('HGETALL', KEYS[1])
            redis.call('DEL', KEYS[1])
            return data
            """);
        redisScript.setResultType(List.class);
        return redisScript;
    }
}
