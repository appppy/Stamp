package jp.osaka.cherry.stamp.android.view.adapter;

import android.view.View;

import java.util.EventListener;


/**
 * アイテムリスナ
 */
public interface ItemListener<T> extends EventListener {

    /**
     * クリック
     *
     * @param view ビュー
     * @param item アイテム
     */
    void onClickMore(View view, T item);

    /**
     * クリック
     *
     * @param view ビュー
     * @param item アイテム
     */
    void onClick(View view, T item);

    /**
     * ロングクリック
     *
     * @param view ビュー
     * @param item アイテム
     */
    void onLongClick(View view, T item);
}
