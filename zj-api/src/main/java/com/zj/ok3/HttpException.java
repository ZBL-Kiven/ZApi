package com.zj.ok3;

import java.util.Objects;

import androidx.annotation.Nullable;

/** Exception for an unexpected, non-2xx HTTP response. */
public class HttpException extends RuntimeException {
  private static String getMessage(Response<?> response) {
    Objects.requireNonNull(response, "response == null");
    return "HTTP " + response.code() + " " + response.message();
  }

  private final int code;
  private final String message;
  private final transient Response<?> response;

  public HttpException(Response<?> response) {
    super(getMessage(response));
    this.code = response.code();
    this.message = response.message();
    this.response = response;
  }

  /** HTTP status code. */
  public int code() {
    return code;
  }

  /** HTTP status message. */
  public String message() {
    return message;
  }

  /** The full HTTP response. This may be null if the exception was serialized. */
  public @Nullable Response<?> response() {
    return response;
  }
}
