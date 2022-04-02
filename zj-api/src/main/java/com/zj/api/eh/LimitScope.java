package com.zj.api.eh;

import androidx.annotation.StringDef;

import com.zj.api.ZApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;


/**
 * Used to limit the marked thread,
 * the scope of thread usage is limited to one of: [Main] [IO] [CALCULATE].
 */
@StringDef({ZApi.IO, ZApi.MAIN, ZApi.CALCULATE})
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
public @interface LimitScope {}
