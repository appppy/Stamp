package jp.osaka.cherry.stamp.utils.timer;

/**
 * タイマーリスナ
 */
public interface TimerListener {

    /**
     * タイマー発火
     *
     * @param timer タイマー
     * @param count タイムアウト回数
     * @param inProgress 進行中
     */
    void onTimer(final Object timer, final int count, final boolean inProgress);
}
