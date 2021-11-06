package jp.osaka.cherry.stamp.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import static jp.osaka.cherry.stamp.constants.STAMP.MAX;


/**
 * 人データベース
 */
class StampDatabase {

    /**
     * @serial プリファレンス
     */
    private final StampStore mPrefs;

    /**
     * @serial パーソン
     */
    private final Collection<SimpleStamp> mCollection = new ArrayList<>();

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    StampDatabase(Context context) {
        mPrefs = new StampStore(context);
    }

    /**
     * リストア
     */
    public void restore() {
        // クリア
        mCollection.clear();
        // リストア
        for (int i = 0; i < MAX; i++) {
            SimpleStamp person = mPrefs.get(String.valueOf(i));
            if (null != person) {
                mCollection.add(person);
            }
        }
    }

    /**
     * バックアップ
     */
    void backup(Collection<SimpleStamp> collection) {
        // バックアップ
        for (int i = 0; i < MAX; i++) {
            mPrefs.clear(String.valueOf(i));
        }
        int position = 0;
        if (collection != null) {
            for (SimpleStamp person : collection) {
                mPrefs.set(String.valueOf(position), person);
                position++;
            }
        }
        mCollection.clear();
        if (collection != null) {
            mCollection.addAll(collection);
        }
    }

    /**
     * スタンプを取得する
     *
     * @return スタンプ
     */
    public Collection<SimpleStamp> getStamp() {
        return mCollection;
    }
}
