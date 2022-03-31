package com.zj.api.uploader.task;

import com.zj.api.progress.ProgressListener;
import com.zj.api.uploader.FileInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

@SuppressWarnings("unused")
class ProgressRequestBody extends RequestBody {
    private final RequestBody requestBody;
    private final FileInfo info;
    private final ProgressListener mListener;

    public ProgressRequestBody(RequestBody body, FileInfo info, ProgressListener listener) {
        this.requestBody = body;
        this.info = info;
        this.mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        if (sink.getClass().getSimpleName().contains("RealBufferedSink")) {
            CountingSink countingSink = new CountingSink(sink);
            BufferedSink bufferedSink = Okio.buffer(countingSink);
            requestBody.writeTo(bufferedSink);
            bufferedSink.flush();
        } else {
            requestBody.writeTo(sink);
        }
    }

    private final class CountingSink extends ForwardingSink {
        long bytesWritten;
        long contentLength;
        int lastProgress = 0;

        CountingSink(Sink delegate) throws IOException {
            super(delegate);
            bytesWritten = 0L;
            contentLength = contentLength();
        }

        @Override
        public void write(@NotNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            int progress = (int) ((bytesWritten * 1.0f / contentLength) * 100);
            if (Math.abs(lastProgress - progress) > 0) {
                lastProgress = progress;
                mListener.onProgress(info, progress, contentLength);
            }
            if (bytesWritten >= contentLength) {
                mListener.onComplete(info, contentLength);
            }
        }
    }
}