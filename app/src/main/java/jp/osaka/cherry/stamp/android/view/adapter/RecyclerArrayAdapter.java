
package jp.osaka.cherry.stamp.android.view.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ArrayAdapterのようなRecycerView.Adapter
 */
public abstract class RecyclerArrayAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * @serial ロック
     */
    private final Object lock = new Object();

    /**
     * @serial オブジェクト
     */
    private final List<T> objects;

    /**
     * コンストラクタ
     *
     * @param objects オブジェクト
     */
    public RecyclerArrayAdapter(List<T> objects) {
        this.objects = objects;
    }

    /**
     * 挿入
     *
     * @param position 位置
     * @param object オブジェクト
     */
    public void insert(int position, @NonNull T object) {
        synchronized (lock) {
            objects.add(position, object);
        }
        notifyItemInserted(position);
    }

    /**
     * 追加
     *
     * @param object オブジェクト
     */
    public void add(@NonNull T object) {
        final int position;
        synchronized (lock) {
            position = objects.size();
            objects.add(object);
        }
        notifyItemInserted(position);
    }

    /**
     * 全設定
     *
     * @param collection コレクション
     */
    public void setAll(@NonNull Collection<? extends T> collection) {
        synchronized (lock) {
            objects.clear();
            objects.addAll(collection);
        }
        notifyDataSetChanged();
    }

    /**
     * 設定
     *
     * @param object オブジェクト
     * @return 位置
     */
    public int set(@NonNull T object) {
        int position = getPosition(object);
        if (position != -1) {
            synchronized (lock) {
                objects.remove(position);
                objects.add(position, object);
            }
            notifyDataSetChanged();
        }
        return position;
    }

    /**
     * 削除
     *
     * @param position ポジション
     * @return 位置
     */
    public T remove(int position) {
        T prev;
        synchronized (lock) {
            prev = objects.remove(position);
        }
        notifyItemRemoved(position);
        return prev;
    }

    /**
     * 移動
     *
     * @param from ここから
     * @param to ここまで
     */
    public void move(int from, int to) {
        synchronized (lock) {
            if (from < to) {
                for (int i = from; i < to; i++) {
                    Collections.swap(objects, i, i + 1);
                }
            } else {
                for (int i = from; i > to; i--) {
                    Collections.swap(objects, i, i - 1);
                }
            }
        }
        notifyItemMoved(from, to);
    }

    /**
     * 位置取得
     *
     * @param object オブジェクト
     * @return 位置
     */
    private int getPosition(T object) {
        int position;
        synchronized (lock) {
            position = objects.indexOf(object);
        }
        return position;
    }

    /**
     * コレクション取得
     *
     * @return コレクション
     */
    public List<T> getCollection() {
        return objects;
    }
}