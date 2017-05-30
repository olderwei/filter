package com.itpuber.filter;

import com.itpuber.domain.FilterRule;

import java.util.List;

/**
 * Created by yoyo on 17/4/24.
 */
public class FilterChain {

    private Filter chain;

    protected FilterChain(List<AbstractFilter> filters, Filter lastFilter) {
        chain = lastFilter;
        if (filters != null) {
            for (int i = filters.size() - 1; i >= 0; i--) {
                AbstractFilter filter = filters.get(i);
                filter.setNext(chain);
                chain = filter;
            }
        }
    }

    public boolean invoke(FilterRule filterRule, Object[] inputParams) {
        return getChain().invoke(filterRule, inputParams);
    }

    public Filter getChain() {
        return chain;
    }

}
