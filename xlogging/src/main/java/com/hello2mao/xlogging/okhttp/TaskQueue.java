package com.hello2mao.xlogging.okhttp;


import android.util.Log;

import com.hello2mao.xlogging.okhttp.util.CharlesUtil;
import com.hello2mao.xlogging.okhttp.bean.HttpTransaction;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskQueue {

    private static final String TAG = "TaskQueue";
    private static int idleCount = 0;
    private static Future dequeueFuture;
    private static final ConcurrentLinkedQueue<HttpTransaction> QUEUE = new ConcurrentLinkedQueue<>();
    private static final ScheduledExecutorService QUEUE_EXECUTOR = Executors
            .newSingleThreadScheduledExecutor();
    private static final Runnable DEQUEUE_TASK = new Runnable() {
        @Override
        public void run() {
            TaskQueue.dequeue();
        }
    };

    public static void start() {
        if (dequeueFuture == null) {
            // 默认周期1s
            dequeueFuture = QUEUE_EXECUTOR.scheduleAtFixedRate(DEQUEUE_TASK, 0L, 1000L, TimeUnit.MILLISECONDS);
            Log.d(TAG, "TaskQueue start");
        }
    }

    private static void stop() {
        if (dequeueFuture != null) {
            dequeueFuture.cancel(true);
            dequeueFuture = null;
            Log.d(TAG, "TaskQueue stop");
        }
    }

    public static void queue(HttpTransaction httpTransaction) {
        QUEUE.add(httpTransaction);
    }

    private static void dequeue() {
        if (QUEUE.size() == 0) {
            idleCount++;
            // 2min内没数据，则把QUEUE_EXECUTOR停掉
            if (idleCount > 120) {
                TaskQueue.stop();
                idleCount = 0;
            }
            return;
        }
        idleCount = 0;
        while (!QUEUE.isEmpty()) {
            try {
                HttpTransaction httpTransaction = QUEUE.remove();
                CharlesUtil.produceHttpTransaction(httpTransaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        CharlesUtil.consumeHttpTransaction();
    }

}
