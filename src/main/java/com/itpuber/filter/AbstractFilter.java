package com.itpuber.filter;

import com.itpuber.domain.FilterRule;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by yoyo on 17/4/24.
 */
public abstract class AbstractFilter implements Filter {

    private Filter next;

    public Filter getNext() {
        return next;
    }

    public void setNext(Filter next) {
        this.next = next;
    }

    protected String generateRequestKey(FilterRule filterRule, Object[] inputParams) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, List<Field>> objectParamsMap = filterRule.getObjectParamsMap();
        boolean isParamAndRuleNotNull = objectParamsMap != null && inputParams != null && inputParams.length != 0;

        if (isParamAndRuleNotNull) {
            for (Object object : inputParams) {
                if (objectParamsMap.containsKey(object.getClass().getName())) {
                    List<Field> fields = objectParamsMap.get(object.getClass().getName());
                    for (Field field : fields) {
                        try {
                            Object fieldValue = field.get(object);
                            if (fieldValue != null) {
                                stringBuilder.append(field.getName() + ":" + fieldValue + "#");
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        List<String> commonParamsList = filterRule.getCommonParamsList();
        if (null != commonParamsList) {
            for (int i = 0; i < commonParamsList.size(); i++) {
                Object objectValue = inputParams[Integer.parseInt(commonParamsList.get(i))];
                if (objectValue != null) {
                    //stringBuilder.append("commonParam:" + objectValue + "#");
                    stringBuilder.append(objectValue);
                }
            }

        }

        return stringBuilder.length() == 0 ? null : stringBuilder.toString();
    }
}
