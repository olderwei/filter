package com.itpuber.core;

import com.alibaba.fastjson.JSON;
import com.itpuber.annotation.TargetMethod;
import com.itpuber.annotation.TargetParam;
import javassist.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yoyo on 17/4/11.
 */
public class SimpleTransformer implements ClassFileTransformer {

    public static Map<String, List<String>> commonParamsMap = new ConcurrentHashMap<String, List<String>>();

    public static Map<String, Map<String, List<Field>>> objectParamsMap = new ConcurrentHashMap<String, Map<String, List<Field>>>();

    public static Map<String, Object> returnTypeMap = new ConcurrentHashMap<String, Object>();

    private static Map<String, Class> ctClassMap = new ConcurrentHashMap<String, Class>();

    private static boolean isInit = false;

    private static List<String> preLoadClassList = new ArrayList<String>();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String newClassName = className.replace("/",".");
        ClassPool classPool = ClassPool.getDefault();
        String classPath = loader.getResource("").getPath();

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(classPath + "/properties/filter.properties"));
            preLoadClassList = Arrays.asList(properties.get("preLoadClass").toString().split(","));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (preLoadClassList.indexOf(newClassName) < 0) {
            return classfileBuffer;
        }

        try {
            CtClass ctClass = classPool.get(newClassName);
            CtMethod[] ctMethods = ctClass.getMethods();
            for (CtMethod ctMethod : ctMethods) {

                Object object = ctMethod.getAnnotation(TargetMethod.class);

                TargetMethod targetMethod = object instanceof TargetMethod ? (TargetMethod)object : null;

                if (targetMethod == null) {
                    continue;
                }

                String filterKey = targetMethod.filterKey();
                long maxCount = targetMethod.maxCount();
                String strategyString = targetMethod.strategy().getValue();
                String timeUnitString = "java.util.concurrent.TimeUnit." + targetMethod.timeUnit().toString();
                String failResult = targetMethod.failResult();

                /** 入参处理 */
                CtClass[] params = ctMethod.getParameterTypes();

                /** 获取参数列表的注解 */
                buildCommonParamsMap(ctMethod, filterKey, params);

                /** 自定义对象参数处理 */
                buildObjectParamsMap(filterKey, params);

                StringBuilder messageBody = new StringBuilder();
                messageBody.append("com.itpuber.domain.FilterRule filterRule = new com.itpuber.domain.FilterRule();");
                messageBody.append("java.util.List commonParamsList = com.itpuber.core.SimpleTransformer.commonParamsMap.get(\"" + filterKey + "\");");
                messageBody.append("java.util.Map objectParamsMap = com.itpuber.core.SimpleTransformer.objectParamsMap.get(\"" + filterKey + "\");");
                messageBody.append("filterRule.setFilterKey(\"" + filterKey + "\");");
                messageBody.append("filterRule.setStrategy(" + strategyString + ");");
                messageBody.append("filterRule.setTimeUnit(" + timeUnitString + ");");
                messageBody.append("filterRule.setMaxCount(" + maxCount + "L);");
                messageBody.append("filterRule.setCommonParamsList(commonParamsList);");
                messageBody.append("filterRule.setObjectParamsMap(objectParamsMap);");
                messageBody.append("com.itpuber.filter.Filter filter = com.itpuber.filter.FilterCache.getFilterInstance(filterRule);");
                messageBody.append("boolean result = filter.invoke(filterRule, $args);");
                messageBody.append("if (!result) { ");
                messageBody.append(buildReturnStatement(filterKey, ctMethod, failResult));
                messageBody.append("}");
                ctMethod.insertBefore(messageBody.toString());
            }

            return ctClass.toBytecode();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    private void buildObjectParamsMap(String filterKey, CtClass[] params) throws ClassNotFoundException, CannotCompileException, NoSuchFieldException {
        Map<String, List<Field>> objectParamFieldMap = objectParamsMap.get(filterKey) == null ? new HashMap<String, List<Field>>() : objectParamsMap.get(filterKey);
        Class clazz = null;
        for (int i = 0; i < params.length; i++) {
            List<Field> fieldList = objectParamFieldMap.get(params[i].getName()) == null ? new ArrayList<Field>() : objectParamFieldMap.get(params[i].getName());
            CtField[] ctFields = params[i].getDeclaredFields();

            for (CtField ctField : ctFields) {
                if (ctField.getAnnotation(TargetParam.class) != null
                        && !params[i].getName().equals("java.util.Map")
                        && !params[i].getName().equals("java.util.List")
                        && !params[i].getName().equals("java.util.Set")) {
                    // TODO
                    /** 将CtClass对象与Class对象的对应关系缓存起来,避免ClassLoader加载该类多次 */
                    if (ctClassMap.containsKey(params[i].getName())) {
                        clazz = ctClassMap.get(params[i].getName());
                    } else {
                        clazz = params[i].toClass();
                        ctClassMap.put(params[i].getName(), clazz);
                    }
                    Field field = clazz.getDeclaredField(ctField.getName());
                    field.setAccessible(true);
                    fieldList.add(field);
                }
            }
            objectParamFieldMap.put(params[i].getName(), fieldList);
        }
        objectParamsMap.put(filterKey, objectParamFieldMap);
    }

    private void buildCommonParamsMap(CtMethod ctMethod, String filterKey, CtClass[] params) throws ClassNotFoundException {
        List<String> commonParamsIndexList = commonParamsMap.get(filterKey) == null ? new ArrayList<String>() : commonParamsMap.get(filterKey);
        Object[][] annotations = ctMethod.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            for (int j = 0; j < annotations[i].length; j++) {
                Object annotation = annotations[i][j];
                if (annotation instanceof TargetParam
                        && !params[i].getName().equals("java.util.Map")
                        && !params[i].getName().equals("java.util.List")
                        && !params[i].getName().equals("java.util.Set")) {
                    commonParamsIndexList.add(String.valueOf(i));

                }
            }
        }
        commonParamsMap.put(filterKey, commonParamsIndexList);
    }

    /**
     * 返回参数处理
     * @param filterKey
     * @param ctMethod
     * @param failResult
     * @return
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private String buildReturnStatement(String filterKey, CtMethod ctMethod, String failResult) throws NotFoundException, CannotCompileException {
        /** 返参处理 */
        StringBuilder statement = new StringBuilder();
        String returnClassName = ctMethod.getReturnType().getName();
        Class returnClazz = ctClassMap.get(returnClassName);
        if (returnClazz == null) {
            if (returnClassName.equals("java.util.Map")
                    || returnClassName.equals("java.util.Set")
                    || returnClassName.equals("java.util.List")) {
                //TODO 如果返回参数为Map,Set,List,暂不支持, 以后是否支持看情况.
                throw new RuntimeException("Return Type not support【Map, List, Set】, method name is " + ctMethod.getName());
            }

            if ("void".equals(returnClassName)) {
                statement.append("return;");
                return statement.toString();
            }

            if ("int".equals(returnClassName)) {
                int tempResult = Integer.parseInt(failResult);
                statement.append(returnClassName + " result =  " + tempResult + ";");
                statement.append("return result;");
                return statement.toString();
            }

            //必须要加new Integer()
            if ("java.lang.Integer".equals(returnClassName)) {
                Integer tempResult = Integer.parseInt(failResult);
                statement.append(returnClassName + " result =  new Integer(" + tempResult + ");");
                statement.append("return result;");
                return statement.toString();
            }

            if ("boolean".equals(returnClassName)) {
                boolean tempResult = Boolean.parseBoolean(failResult);
                statement.append(returnClassName + " result =  " + tempResult + ";");
                statement.append("return result;");
                return statement.toString();
            }

            if ("java.lang.Boolean".equals(returnClassName)) {
                boolean tempResult = Boolean.parseBoolean(failResult);
                statement.append(returnClassName + " result =  new Boolean(" + tempResult + ");");
                statement.append("return result;");
                return statement.toString();
            }

            if ("long".equals(returnClassName)) {
                long tempResult = Long.parseLong(failResult);
                statement.append(returnClassName + " result =  " + tempResult + ";");
                statement.append("return result;");
                return statement.toString();
            }

            if ("java.lang.Long".equals(returnClassName)) {
                long tempResult = Long.parseLong(failResult);
                statement.append(returnClassName + " result =  new Long(" + tempResult + "L);");
                statement.append("return result;");
                return statement.toString();
            }

            if ("java.lang.String".equals(returnClassName)) {
                statement.append("return \"" + failResult + "\";");
                return statement.toString();
            }
            //如果是自定义参数
            returnClazz = ctMethod.getReturnType().toClass();

            ctClassMap.put(ctMethod.getReturnType().getName(), returnClazz);
            Object tempResult = JSON.parseObject(failResult, returnClazz);
            returnTypeMap.put(filterKey, tempResult);

            statement.append(returnClassName + " tempResult = (" + returnClassName + ")com.itpuber.core.SimpleTransformer.returnTypeMap.get(\"" + filterKey +"\");");
            statement.append("return tempResult;");

        } else {
            Object tempResult = JSON.parseObject(failResult, returnClazz);
            returnTypeMap.put(filterKey, tempResult);

            statement.append(returnClassName + " tempResult = (" + returnClassName + ")com.itpuber.core.SimpleTransformer.returnTypeMap.get(\"" + filterKey +"\");");
            statement.append("return tempResult;");
        }

        return statement.toString();
    }
}
