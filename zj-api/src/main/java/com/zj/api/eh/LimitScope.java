package com.zj.api.eh;

import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@StringDef({ErrorHandler.IO, ErrorHandler.MAIN, ErrorHandler.CALCULATE})
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
public @interface LimitScope {}
