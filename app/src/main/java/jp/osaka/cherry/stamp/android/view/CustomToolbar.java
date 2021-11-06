package jp.osaka.cherry.stamp.android.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

/**
 * カスタムバー
 */
public class CustomToolbar extends Toolbar {

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public CustomToolbar(Context context) {
        super(context);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs アトトリビュート
     */
    public CustomToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs アトトリビュート
     * @param defStyleAttr スタイルアトトリビュート
     */
    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        final ViewGroup.LayoutParams params = getLayoutParams();
        if (params instanceof MarginLayoutParams) {
            ((MarginLayoutParams) params).topMargin = insets.top;
        }
        return true;
    }
}

