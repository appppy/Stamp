package jp.osaka.cherry.stamp.utils.controller;


import jp.osaka.cherry.stamp.utils.controller.status.BaseStatus;

/**
 * ワーカーインタフェース
 */
public interface IWorker {

    /**
     * 開始
     *
     * @param command コマンド
     */
    void start(BaseCommand command);

    /**
     * 停止
     */
    void stop();

    /**
     * コールバックインタフェース
     */
    interface Callbacks {
        /**
         * コマンド開始通知
         *
         * @param worker ワーカー
         * @param command コマンド
         */
        void onStarted(IWorker worker, BaseCommand command);

        /**
         * コマンド更新通知
         *
         * @param worker ワーカー
         * @param command コマンド
         * @param status 状態
         */
        void onUpdated(IWorker worker, BaseCommand command, BaseStatus status);

        /**
         * 成功通知
         *
         * @param worker ワーカー
         * @param command コマンド
         */
        void onSuccessed(IWorker worker, BaseCommand command);

    }

}
