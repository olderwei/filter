package com.itpuber.filter;

import com.itpuber.domain.FilterRule;

/**
 * Created by yoyo on 17/4/23.
 */
public interface Filter {

    public boolean invoke(FilterRule filterRule, Object[] inputParams);
}
