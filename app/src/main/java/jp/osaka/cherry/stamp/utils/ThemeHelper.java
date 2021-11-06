package jp.osaka.cherry.stamp.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

import jp.osaka.cherry.stamp.R;
import jp.osaka.cherry.stamp.constants.COLOR;


/**
 * テーマヘルパ
 */
public class ThemeHelper {

    /**
     * イメージ画像取得
     *
     * @param context コンテキスト
     * @param color カラー
     * @return イメージ画像
     */
    public static int getImageResource(Context context, COLOR color) {
        if (context == null || color == null) {
            return R.drawable.ic_lens_grey_300_24dp;
        }
        int result = R.drawable.ic_lens_grey_300_24dp;
        try {
            switch (color) {
                case RED: {
                    result = R.drawable.ic_lens_red_24dp;
                    break;
                }
                case BLUE: {
                    result = R.drawable.ic_lens_blue_24dp;
                    break;
                }
                case YELLOW: {
                    result = R.drawable.ic_lens_yellow_24dp;
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * ラインカラー取得
     *
     * @param context コンテキスト
     * @param color カラー
     * @return ラインカラー
     */
    public static int getLineColor(Context context, COLOR color) {
        if (context == null || color == null) {
            return 0;
        }
        return ContextCompat.getColor(context, R.color.grey_100);
    }

    /**
     * 選択ラインカラー取得
     *
     * @param context コンテキスト
     * @param color カラー
     * @return 選択ラインカラー
     */
    public static int getSelectedLineColor(Context context, COLOR color) {
        if (context == null || color == null) {
            return 0;
        }
        return ContextCompat.getColor(context, R.color.grey_300);
    }

    /**
     * テーマ取得
     *
     * @param color カラー
     * @return テーマ
     */
    public static int getTheme(COLOR color) {
        int result = R.style.AppTheme_Grey;
        try {
            switch (color) {
                case RED: {
                    result = R.style.AppTheme_Red;
                    break;
                }
                case BLUE: {
                    result = R.style.AppTheme_Blue;
                    break;
                }
                case YELLOW: {
                    result = R.style.AppTheme_Yellow;
                    break;
                }
                default:
                case GREY: {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
