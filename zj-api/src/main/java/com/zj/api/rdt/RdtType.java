package com.zj.api.rdt;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.zj.api.rdt.RdtModKt.ALWAYS;
import static com.zj.api.rdt.RdtModKt.NEVER;
import static com.zj.api.rdt.RdtModKt.CLEAR_OBSERVER;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@IntDef({ALWAYS, CLEAR_OBSERVER, NEVER})
public @interface RdtType {}
