package jp.osaka.cherry.stamp.utils;

import android.annotation.SuppressLint;

import jp.osaka.cherry.stamp.R;
import jp.osaka.cherry.stamp.ui.ArchiveActivity;
import jp.osaka.cherry.stamp.ui.BlueActivity;
import jp.osaka.cherry.stamp.ui.MainActivity;
import jp.osaka.cherry.stamp.ui.RedActivity;
import jp.osaka.cherry.stamp.ui.TrashActivity;
import jp.osaka.cherry.stamp.ui.YellowActivity;


/**
 * アクティビティ遷移ヘルパ
 */
public class ActivityTransitionHelper {

    /**
     * 開始アクティビティの取得
     *
     * @param id 識別子
     * @return 開始アクティビティ
     */
    @SuppressLint("NonConstantResourceId")
    public static Class<?> getStartActivity(int id) {
        Class<?> c = MainActivity.class;

        try {
            switch (id) {
                case R.id.stamp: {
                    c = MainActivity.class;
                    break;
                }
                case R.id.red: {
                    c = RedActivity.class;
                    break;
                }
                case R.id.blue: {
                    c = BlueActivity.class;
                    break;
                }
                case R.id.yellow: {
                    c = YellowActivity.class;
                    break;
                }
                case R.id.archive: {
                    c = ArchiveActivity.class;
                    break;
                }
                case R.id.trash: {
                    c = TrashActivity.class;
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }
}
