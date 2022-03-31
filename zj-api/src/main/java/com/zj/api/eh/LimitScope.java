package com.zj.api.eh;

import androidx.annotation.StringDef;

import com.zj.api.ZApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@StringDef({ZApi.IO, ZApi.MAIN, ZApi.CALCULATE})
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
public @interface LimitScope {}
