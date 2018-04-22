package com.itpuber.filter;

import com.itpuber.domain.FilterRule;
import com.itpuber.enums.Strategy;
import com.itpuber.filter.impl.GuavaRateLimitFilter;
import com.itpuber.filter.impl.GuavaUnitLimitFilter;
import com.itpuber.filter.impl.PercentLimitFilter;
import com.itpuber.filter.impl.RedisUnitLimitFilter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yoyo on 17/5/20.
 */
public class FilterCache {

    private static Map<String, Filter> filterCache = new ConcurrentHashMap<String, Filter>();

    private static Lock lock = new ReentrantLock();

    public static Filter getFilterInstance(FilterRule filterRule) {
        String filterKey = filterRule.getFilterKey();

        if (filterCache.get(filterKey) != null) {
            return filterCache.get(filterKey);
        }

        try {
            lock.lock();

            if (filterCache.get(filterKey) != null) {
                return filterCache.get(filterKey);
            }

            if (filterRule.getStrategy().equals(Strategy.GUAVA_UNIT_LIMIT)) {
                GuavaUnitLimitFilter guavaUnitLimitFilter = new GuavaUnitLimitFilter();
                filterCache.put(filterKey, guavaUnitLimitFilter);
                return guavaUnitLimitFilter;
            } else if (filterRule.getStrategy().equals(Strategy.GUAVA_RATE_LIMIT)) {
                GuavaRateLimitFilter guavaRateLimitFilter = new GuavaRateLimitFilter();
                filterCache.put(filterKey, guavaRateLimitFilter);
                return guavaRateLimitFilter;
            } else if (filterRule.getStrategy().equals(Strategy.PERCENT_LIMIT)) {
                PercentLimitFilter percentLimitFilter = new PercentLimitFilter();
                filterCache.put(filterKey, percentLimitFilter);
                return percentLimitFilter;
            } else if (filterRule.getStrategy().equals(Strategy.REDIS)) {
                RedisUnitLimitFilter redisUnitLimitFilter = new RedisUnitLimitFilter();
                filterCache.put(filterKey, redisUnitLimitFilter);
                return redisUnitLimitFilter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return null;
    }
}
