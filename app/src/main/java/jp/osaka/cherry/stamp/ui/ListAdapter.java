package jp.osaka.cherry.stamp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jp.osaka.cherry.stamp.R;
import jp.osaka.cherry.stamp.android.view.adapter.ItemListener;
import jp.osaka.cherry.stamp.android.view.adapter.RecyclerArrayAdapter;
import jp.osaka.cherry.stamp.databinding.ItemLineBinding;
import jp.osaka.cherry.stamp.service.SimpleStamp;

import static jp.osaka.cherry.stamp.utils.SelectHelper.isMultiSelected;
import static jp.osaka.cherry.stamp.utils.SelectHelper.isSelected;
import static jp.osaka.cherry.stamp.utils.ThemeHelper.getImageResource;
import static jp.osaka.cherry.stamp.utils.ThemeHelper.getLineColor;
import static jp.osaka.cherry.stamp.utils.ThemeHelper.getSelectedLineColor;

/**
 * リストアダプタ
 */
class ListAdapter extends RecyclerArrayAdapter<SimpleStamp, ListAdapter.BindingHolder> {

    /**
     * @serial コンテキスト
     */
    private final Context mContext;

    /**
     * @serial レイアウトインフレータ
     */
    private final LayoutInflater mInflater;

    /**
     * @serial リスナ
     */
    private final ItemListener<SimpleStamp> mListener;

    /**
     * @serial アイテムタッチへルパ
     */
    private ItemTouchHelper mItemTouchHelper;

    /**
     * @serial タッチビューホルダー
     */
    private final Collection<View> touchViewHolder = new ArrayList<>();

    /**
     * コンストラクタ
     *
     * @param context    コンテキスト
     * @param listener   リスナ
     * @param collection コレクション
     */
    ListAdapter(Context context, ItemListener<SimpleStamp> listener, List<SimpleStamp> collection) {
        super(collection);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    /**
     * タッチへルパの設定
     *
     * @param helper ヘルパ
     */
    void setItemTouchHelper(ItemTouchHelper helper) {
        mItemTouchHelper = helper;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public BindingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder(mInflater.inflate(R.layout.item_line, parent, false));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final BindingHolder holder, int position) {
        // アイテム
        final SimpleStamp item = getCollection().get(position);
        if (item != null) {
            // タッチの設定
            holder.getBinding().cardView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        touchViewHolder.clear();
                        touchViewHolder.add(view);
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (isSelected(getCollection())) {
                            for (View holdview : touchViewHolder) {
                                if (holdview.equals(view)) {
                                    // 選択
                                    select(view, holder, item);
                                }
                            }
                        } else {
                            for (View holdview : touchViewHolder) {
                                if (holdview.equals(view)) {
                                    // 短押し
                                    mListener.onClick(view, item);
                                }
                            }
                        }
                        touchViewHolder.clear();
                    }
                    return false;
                }
            });
            // 短押しの設定
            holder.getBinding().cardView.setOnClickListener(new View.OnClickListener() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onClick(View v) {
                    // 処理なし
                }
            });
            // 長押しの設定
            holder.getBinding().cardView.setOnLongClickListener(new View.OnLongClickListener() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean onLongClick(View v) {
                    // 選択
                    select(v, holder, item);
                    // タッチビューホルダをクリア
                    touchViewHolder.clear();
                    return true;
                }
            });
            // アイテムの選択状態の取得
            if (item.isSelected) {
                // アイコンの設定
                holder.getBinding().icon.setImageResource(R.drawable.ic_check_circle_grey_600_24dp);
                // 背景の設定
                holder.getBinding().cardView.setBackgroundColor(getSelectedLineColor(mContext, item.color));
            } else {
                // アイコンの設定
                holder.getBinding().icon.setImageResource(getImageResource(mContext, item.color));
                // 背景の設定
                holder.getBinding().cardView.setBackgroundColor(getLineColor(mContext, item.color));
            }
            // タイトル
            holder.getBinding().title.setText(DateFormat.getDateTimeInstance().format(new Date(item.creationDate)));
            // Moreの設定
            holder.getBinding().buttonPopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isSelected(getCollection())) {
                        // 短押し
                        mListener.onClickMore(view, item);
                    }
                }
            });
        }
    }

    /**
     * Returns the total number of items in the data change hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return getCollection().size();
    }

    /**
     * 選択
     *
     * @param view   ビュー
     * @param holder ホルダー
     * @param item   アイテム
     */
    private void select(View view, BindingHolder holder, SimpleStamp item) {
        if (mItemTouchHelper != null) {
            // 選択状態を変更
            item.isSelected = !item.isSelected;
            // 複数選択の場合、
            if (isMultiSelected(getCollection())) {
                if (item.isSelected) {
                    // アイコンの設定
                    holder.getBinding().icon.setImageResource(R.drawable.ic_check_circle_grey_600_24dp);
                    // 背景の設定
                    holder.getBinding().cardView.setBackgroundColor(getSelectedLineColor(mContext, item.color));
                } else {
                    // アイコンの設定
                    holder.getBinding().icon.setImageResource(getImageResource(mContext, item.color));
                    // 背景の設定
                    holder.getBinding().cardView.setBackgroundColor(getLineColor(mContext, item.color));
                }
            } else {
                if (item.isSelected) {
                    // ドラッグを開始する
                    mItemTouchHelper.startDrag(holder);
                } else {
                    // アイコンの設定
                    holder.getBinding().icon.setImageResource(getImageResource(mContext, item.color));
                    // 背景の設定
                    holder.getBinding().cardView.setBackgroundColor(getLineColor(mContext, item.color));
                }
            }
            // 長押し
            mListener.onLongClick(view, item);
        }
    }

    /**
     * ビューホルダ
     */
    static class BindingHolder extends RecyclerView.ViewHolder {
        private final ItemLineBinding mBinding;
        BindingHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
        public ItemLineBinding getBinding() {
            return mBinding;
        }
    }

}

