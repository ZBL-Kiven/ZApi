package com.zj.ok3;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;


// This class conforms to the annotation requirements documented on Annotation.
@SuppressWarnings("ClassExplicitlyAnnotation")
final class SkipCallbackExecutorImpl implements com.zj.ok3.SkipCallbackExecutor {

  private static final com.zj.ok3.SkipCallbackExecutor INSTANCE = new SkipCallbackExecutorImpl();

  static Annotation[] ensurePresent(Annotation[] annotations) {
    if (Utils.isAnnotationPresent(annotations, com.zj.ok3.SkipCallbackExecutor.class)) {
      return annotations;
    }

    Annotation[] newAnnotations = new Annotation[annotations.length + 1];
    // Place the skip annotation first since we're guaranteed to check for it in the call adapter.
    newAnnotations[0] = SkipCallbackExecutorImpl.INSTANCE;
    System.arraycopy(annotations, 0, newAnnotations, 1, annotations.length);
    return newAnnotations;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return com.zj.ok3.SkipCallbackExecutor.class;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof com.zj.ok3.SkipCallbackExecutor;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @NonNull
  @Override
  public String toString() {
    return "@" + SkipCallbackExecutor.class.getName() + "()";
  }
}
