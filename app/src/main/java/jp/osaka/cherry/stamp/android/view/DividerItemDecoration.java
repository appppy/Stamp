package jp.osaka.cherry.stamp.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 水平線
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * @serial アトトリビュート
     */
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    /**
     * @serial 水平線
     */
    private final Drawable mDivider;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public DividerItemDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        drawVertical(c, parent);
    }

    /**
     * 水平描く
     *
     * @param c キャンバス
     * @param parent 親ビュー
     */
    private void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }
}