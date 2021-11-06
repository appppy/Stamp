// IStampServiceCallback.aidl
package jp.osaka.cherry.stamp.service;

import jp.osaka.cherry.stamp.service.SimpleStamp;

interface IStampServiceCallback {
    /**
     * 更新
     */
    void update(in List<SimpleStamp> stamp);
}
