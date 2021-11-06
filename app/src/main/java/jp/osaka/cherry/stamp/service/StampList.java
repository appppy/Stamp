package jp.osaka.cherry.stamp.service;

import java.util.Collection;

import jp.osaka.cherry.stamp.utils.controller.status.BaseStatus;


/**
 * ブック
 */
class StampList extends BaseStatus {

    /**
     * @serial コレクション
     */
    public Collection<SimpleStamp> collection;

    /**
     * コンストラクタ
     *
     * @param collection コレクション
     */
    StampList(Collection<SimpleStamp> collection) {
        this.collection = collection;
    }

}
