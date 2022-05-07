package com.zj.api.eh;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to limit the marked thread,
 * the scope of thread usage is limited to one of: [Main] [IO] [CALCULATE].
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
public @interface EHParams {
    String value();
}
