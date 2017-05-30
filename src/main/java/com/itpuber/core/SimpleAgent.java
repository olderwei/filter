package com.itpuber.core;

import java.lang.instrument.Instrumentation;

/**
 * Created by yoyo on 17/4/11.
 */
public class SimpleAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        instrumentation.addTransformer(new SimpleTransformer());
    }
}
