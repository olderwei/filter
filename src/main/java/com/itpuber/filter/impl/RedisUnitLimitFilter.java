package com.itpuber.filter.impl;

import com.itpuber.domain.FilterRule;
import com.itpuber.filter.AbstractFilter;
import redis.clients.jedis.Jedis;

/**
 * Created by chenwei on 2018/4/8.
 */
public class RedisUnitLimitFilter extends AbstractFilter {

    Jedis  jedis = new Jedis("localhost", 6379);

    public boolean invoke(FilterRule filterRule, Object[] inputParams) {
        String requestKey = generateRequestKey(filterRule, inputParams);
        requestKey = requestKey == null ? filterRule.getFilterKey() : requestKey;

        final long maxCount = filterRule.getMaxCount();
        try {
            Long total = jedis.incr(requestKey);
            if (total > maxCount) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
