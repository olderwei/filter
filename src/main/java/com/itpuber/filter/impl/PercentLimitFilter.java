package com.itpuber.filter.impl;

import com.itpuber.domain.FilterRule;
import com.itpuber.filter.AbstractFilter;

import java.util.Calendar;

/**
 * Created by chenwei on 2017/6/7.
 */
public class PercentLimitFilter extends AbstractFilter {

    //TODO 可配置， 限流的百分比
    private int limitPercent = 30;

    //TODO 可配置， 多长时间切换一下时间窗口
    private int rangeTime = 1;

    public boolean invoke(FilterRule filterRule, Object[] inputParams) {
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        //一个小时内的第几个时间段,从0开始到9结束
        int minuteGroup = (minute / rangeTime ) % 10 + 1;

        int requestKeyHashCode = generateRequestKey(filterRule, inputParams).hashCode();
        int addNum = 100 - minuteGroup * limitPercent;
        if ((requestKeyHashCode + addNum) % 100 < limitPercent) {
            return false;
        }

        return true;
    }
}
