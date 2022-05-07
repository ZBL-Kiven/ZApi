package com.zj.ok3;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import androidx.annotation.Nullable;
import kotlin.Unit;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import com.zj.ok3.http.Streaming;

final class BuiltInConverters extends Converter.Factory {
  /** Not volatile because we don't mind multiple threads discovering this. */
  private boolean checkForKotlinUnit = true;

  @Override
  public @Nullable Converter<ResponseBody, ?> responseBodyConverter(
      Type type, Annotation[] annotations, ZHttpServiceCreator ZHttpServiceCreator) {
    if (type == ResponseBody.class) {
      return Utils.isAnnotationPresent(annotations, Streaming.class)
          ? StreamingResponseBodyConverter.INSTANCE
          : BufferingResponseBodyConverter.INSTANCE;
    }
    if (type == Void.class) {
      return VoidResponseBodyConverter.INSTANCE;
    }
    if (checkForKotlinUnit) {
      try {
        if (type == Unit.class) {
          return UnitResponseBodyConverter.INSTANCE;
        }
      } catch (NoClassDefFoundError ignored) {
        checkForKotlinUnit = false;
      }
    }
    return null;
  }

  @Override
  public @Nullable Converter<?, RequestBody> requestBodyConverter(
      Type type,
      Annotation[] parameterAnnotations,
      Annotation[] methodAnnotations,
      ZHttpServiceCreator ZHttpServiceCreator) {
    if (RequestBody.class.isAssignableFrom(Utils.getRawType(type))) {
      return RequestBodyConverter.INSTANCE;
    }
    return null;
  }

  static final class VoidResponseBodyConverter implements Converter<ResponseBody, Void> {
    static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();

    @Override
    public Void convert(ResponseBody value) {
      value.close();
      return null;
    }
  }

  static final class UnitResponseBodyConverter implements Converter<ResponseBody, Unit> {
    static final UnitResponseBodyConverter INSTANCE = new UnitResponseBodyConverter();

    @Override
    public Unit convert(ResponseBody value) {
      value.close();
      return Unit.INSTANCE;
    }
  }

  static final class RequestBodyConverter implements Converter<RequestBody, RequestBody> {
    static final RequestBodyConverter INSTANCE = new RequestBodyConverter();

    @Override
    public RequestBody convert(RequestBody value) {
      return value;
    }
  }

  static final class StreamingResponseBodyConverter
      implements Converter<ResponseBody, ResponseBody> {
    static final StreamingResponseBodyConverter INSTANCE = new StreamingResponseBodyConverter();

    @Override
    public ResponseBody convert(ResponseBody value) {
      return value;
    }
  }

  static final class BufferingResponseBodyConverter
      implements Converter<ResponseBody, ResponseBody> {
    static final BufferingResponseBodyConverter INSTANCE = new BufferingResponseBodyConverter();

    @Override
    public ResponseBody convert(ResponseBody value) throws IOException {
      try {
        // Buffer the entire body to avoid future I/O.
        return Utils.buffer(value);
      } finally {
        value.close();
      }
    }
  }

  static final class ToStringConverter implements Converter<Object, String> {
    static final ToStringConverter INSTANCE = new ToStringConverter();

    @Override
    public String convert(Object value) {
      return value.toString();
    }
  }
}
