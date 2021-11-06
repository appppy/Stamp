package jp.osaka.cherry.stamp.utils;

import java.util.ArrayList;
import java.util.List;

import jp.osaka.cherry.stamp.service.SimpleStamp;

import static jp.osaka.cherry.stamp.constants.STAMP.MAX;

/**
 * ヘルパ
 */
public class StampHelper {

    /**
     * 最大数の確認
     *
     * @param list リスト
     * @return 最大の有無
     */
    public static boolean isMax(List<SimpleStamp> list) {
        boolean result = false;
        try {
            if (list.size() >= MAX) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * アイテムの取得
     *
     * @param uuid 識別子
     * @param list リスト
     * @return アイテム
     */
    public static SimpleStamp getStamp(String uuid, List<SimpleStamp> list) {
        SimpleStamp result = null;
        try {
            for (SimpleStamp dest : list) {
                if (uuid.equals(dest.uuid)) {
                    result = dest;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * リストのコピー
     *
     * @param dest コピー先
     * @param src  コピー元
     */
    public static void copyStampList(ArrayList<SimpleStamp> dest, ArrayList<SimpleStamp> src) {
        try {
            dest.clear();
            for (SimpleStamp s : src) {
                SimpleStamp d = SimpleStamp.createInstance();
                d.copyStamp(s);
                dest.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 編集の有無
     *
     * @param dest 比較先
     * @param src 比較元
     * @return 編集の有無
     */
    public static boolean isModified(SimpleStamp dest, SimpleStamp src) {
        boolean result = false;
        try {
            if (dest.uuid.equals(src.uuid)) {
                if ((dest.creationDate != src.creationDate)
                        || (dest.modifiedDate != src.modifiedDate)
                        || (dest.isArchive != src.isArchive)
                        || (dest.isTrash != src.isTrash)) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return !result;
    }
}
