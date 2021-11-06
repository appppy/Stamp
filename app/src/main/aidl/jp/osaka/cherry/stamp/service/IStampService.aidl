// IStampService.aidl
package jp.osaka.cherry.stamp.service;

import jp.osaka.cherry.stamp.service.IStampServiceCallback;
import jp.osaka.cherry.stamp.service.SimpleStamp;

interface IStampService {
    /**
     * コールバック登録
     */
    void registerCallback(IStampServiceCallback callback);

    /**
     * コールバック解除
     */
    void unregisterCallback(IStampServiceCallback callback);

    /**
     * 設定
     */
    void setStamp(in List<SimpleStamp> stamp);

    /**
     * 取得
     */
    void getStamp();
}
