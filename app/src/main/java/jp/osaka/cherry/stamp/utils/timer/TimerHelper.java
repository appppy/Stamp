package jp.osaka.cherry.stamp.utils.timer;

/**
 * タイマヘルパ
 */
public class TimerHelper {

    /**
     * タイマ生成
     *
     * @param timer タイマ
     * @param timeout タイムアウト
     * @param listener リスナ
     * @return タイマ
     */
    static public SimpleTimer createTimer(SimpleTimer timer, int timeout, TimerListener listener) {
        if (timer == null) {
            timer = new SimpleTimer(timeout, listener);
        }
        return timer;
    }

    /**
     * タイマ開始
     *
     * @param timer タイマ
     */
    static public void startTimer(SimpleTimer timer) {
        if (timer != null) {
            if (timer.isActive()) {
                timer.restart();
            } else {
                timer.start();
            }
        }
    }

    /**
     * タイマ停止
     *
     * @param timer タイマ
     */
    static public void stopTimer(SimpleTimer timer) {
        if (timer != null) {
            if (timer.isActive()) {
                timer.stop();
            }
        }
    }
}
