package com.zj.api.mock;

import androidx.annotation.Nullable;

import com.zj.api.eh.EHParam;

public interface MockAble<T> {

    @Nullable
    T getMockData(EHParam ehParam);

}
