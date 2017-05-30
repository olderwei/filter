package com.itpuber.domain;

import com.itpuber.enums.Strategy;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yoyo on 17/5/20.
 */
public class FilterRule {

    private String filterKey;

    private Strategy strategy;

    private TimeUnit timeUnit;

    private long maxCount;

    private List<String> commonParamsList;

    private Map<String, List<Field>> objectParamsMap;

    public FilterRule() {

    }

    public FilterRule(String filterKey, Strategy strategy, TimeUnit timeUnit, long maxCount) {
        this.filterKey = filterKey;
        this.strategy = strategy;
        this.timeUnit = timeUnit;
        this.maxCount = maxCount;
    }

    public String getFilterKey() {
        return filterKey;
    }

    public void setFilterKey(String filterKey) {
        this.filterKey = filterKey;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
    }

    public List<String> getCommonParamsList() {
        return commonParamsList;
    }

    public void setCommonParamsList(List<String> commonParamsList) {
        this.commonParamsList = commonParamsList;
    }

    public Map<String, List<Field>> getObjectParamsMap() {
        return objectParamsMap;
    }

    public void setObjectParamsMap(Map<String, List<Field>> objectParamsMap) {
        this.objectParamsMap = objectParamsMap;
    }
}
