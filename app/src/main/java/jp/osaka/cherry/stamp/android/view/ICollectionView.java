package jp.osaka.cherry.stamp.android.view;

import android.view.View;

import java.util.Collection;

/**
 * コレクションビューのインタフェース
 */
public interface ICollectionView<T> {

    /**
     * アイテム挿入
     *
     * @param item アイテム
     */
    void insert(int index, T item);

    /**
     * アイテム追加
     *
     * @param item アイテム
     */
    void add(T item);

    /**
     * アイテム削除
     *
     * @param item アイテム
     * @return 削除したアイテム位置
     */
    int remove(T item);

    /**
     * 選択解除
     */
    void diselect();

    /**
     * 全選択
     */
    Collection<? extends T> selectedAll();

    /**
     * アイテム変更通知
     *
     * @param item アイテム
     * @return 変更前のアイテムの位置
     */
    int change(T item);

    /**
     * データセット変更通知
     *
     * @param collection コレクション
     */
    void changeAll(Collection<? extends T> collection);

    /**
     * @serial コールバック定義
     */
    interface Callbacks<T> {
        /**
         * アイテムのポップアップメニュー選択
         *
         * @param collectionView コレクションビュー
         * @param view           アイテムビュー
         * @param item           アイテム
         */
        void onSelectedMore(ICollectionView<T> collectionView, View view, T item);

        /**
         * アイテムの選択
         *
         * @param collectionView コレクションビュー
         * @param view アイテムビュー
         * @param item アイテム
         */
        void onSelected(ICollectionView<T> collectionView, View view, T item);

        /**
         * アイテムの選択状態の変更
         *
         * @param collectionView コレクションビュー
         * @param view アイテムビュー
         * @param item アイテム
         * @param collection コレクション
         */
        void onSelectedChanged(ICollectionView<T> collectionView, View view, T item, Collection<? extends T> collection);

        /**
         * アイテムのスワイプ
         *
         * @param collectionView コレクションビュー
         * @param item アイテム
         */
        void onSwiped(ICollectionView<T> collectionView, T item);

        /**
         * スクロール開始
         *
         * @param view コレクションビュー
         */
        void onScroll(ICollectionView<T> view);

        /**
         * スクロール終了
         *
         * @param view コレクションビュー
         */
        void onScrollFinished(ICollectionView<T> view);

        /**
         * 移動変化通知
         *
         * @param view ビュー
         * @param collection コレクション
         */
        void onMoveChanged(ICollectionView<T> view, Collection<? extends T> collection);

        /**
         * 更新通知
         *
         * @param view ビュー
         * @param collection コレクション
         */
        void onUpdated(ICollectionView<T> view, Collection<? extends T> collection);
    }
}
