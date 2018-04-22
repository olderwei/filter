package com.itpuber.filter.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itpuber.domain.FilterRule;
import com.itpuber.filter.AbstractFilter;
import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yoyo on 17/4/24.
 */
public class GuavaUnitLimitFilter extends AbstractFilter {

    private static Cache<String, AtomicLong> cache = CacheBuilder.newBuilder().maximumSize(1000).build();

    private static final Logger logger = LoggerFactory.getLogger(GuavaRateLimitFilter.class);

    public boolean invoke(FilterRule filterRule, Object[] inputParams) {
        String requestKey = generateRequestKey(filterRule, inputParams);
        requestKey = requestKey == null ? filterRule.getFilterKey() : requestKey;

        final long maxCount = filterRule.getMaxCount();
        try {
            AtomicLong requestTotal = cache.get(requestKey, new Callable<AtomicLong>() {
                public AtomicLong call() {
                    return new AtomicLong(maxCount);
                }
            });
            if (requestTotal.decrementAndGet() < 0) {
                return false;
            }

        } catch (ExecutionException e) {
            logger.error("GuavaUnitLimitFilter execute error", e);
        }

        return true;
    }
}
