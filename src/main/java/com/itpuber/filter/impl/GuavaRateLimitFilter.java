package com.itpuber.filter.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.itpuber.domain.FilterRule;
import com.itpuber.filter.AbstractFilter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yoyo on 17/5/21.
 */
public class GuavaRateLimitFilter extends AbstractFilter {

    private static Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<String, RateLimiter>();

    private static Lock lock = new ReentrantLock();

    public boolean invoke(FilterRule filterRule, Object[] inputParams) {
        String requestKey = generateRequestKey(filterRule, inputParams);
        requestKey = requestKey == null ? filterRule.getFilterKey() : requestKey;
        //TODO 考虑HashMap的容量
        RateLimiter limit = getRateLimit(requestKey, filterRule.getMaxCount());
        return limit.tryAcquire();
    }

    public RateLimiter getRateLimit(String resource, long maxCount) {
        RateLimiter limit = rateLimiterMap.get(resource);
        if (limit == null) {
            try {
                lock.lock();
                limit = rateLimiterMap.get(resource);
                if (limit == null) {
                    limit = RateLimiter.create(maxCount);
                    rateLimiterMap.put(resource, limit);
                }
            }  finally {
                lock.unlock();
            }
        }
        return rateLimiterMap.get(resource);
    }
}
