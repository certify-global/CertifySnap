package com.certify.snap.async;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncTaskExecutorService {
    private static final String TAG = AsyncTaskExecutorService.class.getSimpleName();
    private int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private int KEEP_ALIVE_TIME = 1;
    private TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    ExecutorService executorService = new ThreadPoolExecutor(NUMBER_OF_CORES,
            NUMBER_OF_CORES*2,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            taskQueue,
            new BackgroundThreadFactory());

    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread" + sTag);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e(TAG, thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
