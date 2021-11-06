package jp.osaka.cherry.stamp.utils.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 単発タイマー
 */
public class SimpleTimer {

    /**
     * @serial インスタンス
     */
    private final SimpleTimer mSelf;

    /**
     * @serial 実行
     */
    private ScheduledExecutorService mExecutor;

    /**
     * @serial 未来
     */
    private ScheduledFuture<?> mFuture;

    /**
     * @serial リスナー
     */
    private final TimerListener mListener;

    /**
     * @serial タイムアウト時間
     */
    private long mTimeOut;

    /**
     * @serial カウンタ
     */
    private int mCount = 0;

    /**
     * コンストラクタ
     *
     * @param msec タイムアウト時間
     * @param listener リスナー
     */
    public SimpleTimer(final long msec, final TimerListener listener) {
        mTimeOut = msec;
        mListener = listener;
        mSelf = this;
    }

    /**
     * 開始
     */
    public void start() {
        if (null != mFuture) {
            if (!mFuture.isDone()) {
                return;
            }
        }
        startUpTimer();
    }

    /**
     * 再開
     */
    public void restart() {
        restart(mTimeOut);
    }

    /**
     * 再開
     *
     * @param msec タイムアウト
     */
    public void restart(final long msec) {
        stop();
        mCount = 0;
        mTimeOut = msec;
        startUpTimer();
    }

    /**
     * 停止
     */
    public void stop() {
        if (null != mExecutor) {
            if (!mExecutor.isShutdown()) {
                mExecutor.shutdownNow();
            }
            mExecutor = null;
        }
        if (null != mFuture) {
            mFuture = null;
        }
        mCount = 0;
    }

    /**
     * 起動状態取得
     *
     * @return 起動状態取得
     */
    public boolean isActive() {
        boolean ret = false;
        if (null != mFuture) {
            if (!mFuture.isDone()) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 起動
     */
    private void startUpTimer() {
        try {
            if (null == mExecutor) {
                // タイマー生成
                mExecutor = Executors.newSingleThreadScheduledExecutor();
            }
            mFuture = mExecutor.schedule(mJob, mTimeOut, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @serial 実行
     */
    private final Runnable mJob = new Runnable() {

        /**
         * (non-Javadoc)
         * 
         * @see Runnable#run()
         */
        @Override
        public void run() {
            mListener.onTimer(mSelf, ++mCount, false);
            mCount++;
        }
    };

}
