package com.zj.ok3;

/**
 * Communicates responses from a server or offline requests. One and only one method will be invoked
 * in response to a given request.
 *
 * <p>Callback methods are executed using the {@link ZHttpServiceCreator} callback executor. When none is
 * specified, the following defaults are used:
 *
 * <ul>
 *   <li>Android: Callbacks are executed on the application's main (UI) thread.
 *   <li>JVM: Callbacks are executed on the background thread which performed the request.
 * </ul>
 *
 * @param <T> Successful response body type.
 */
public interface Callback<T> {
  /**
   * Invoked for a received HTTP response.
   *
   * <p>Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
   * Call {@link Response#isSuccessful()} to determine if the response indicates success.
   */
  void onResponse(Call<T> call, Response<T> response);

  /**
   * Invoked when a network exception occurred talking to the server or when an unexpected exception
   * occurred creating the request or processing the response.
   */
  void onFailure(Call<T> call, Throwable t);
}
