package jp.osaka.cherry.stamp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.osaka.cherry.stamp.service.SimpleStamp;


/**
 * 選択ヘルパ
 */
public class SelectHelper {

    /**
     * 選択状態の確認
     *
     * @return 選択状態
     */
    public static boolean isSelected(List<SimpleStamp> collection) {
        boolean result = false;
        try {
            // 製品の選択状態を確認する
            for (SimpleStamp person : collection) {
                // 選択状態を確認した
                if (person.isSelected) {
                    result = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 複数選択状態の確認
     *
     * @return 複数選択状態
     */
    public static boolean isMultiSelected(List<SimpleStamp> collection) {
        int count = 0;
        try {
            // 製品の選択状態を確認する
            for (SimpleStamp person : collection) {
                // 選択状態を確認した
                if (person.isSelected) {
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (count > 1);
    }

    /**
     * 選択リストの取得
     *
     * @return 選択リスト
     */
    public static Collection<SimpleStamp> getSelectedCollection(List<SimpleStamp> list) {
        Collection<SimpleStamp> collection = new ArrayList<>();
        try {
            for (SimpleStamp person : list) {
                if (person.isSelected) {
                    collection.add(person);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collection;
    }

    /**
     * 選択リストの取得
     *
     * @return 選択リスト
     */
    public static Collection<SimpleStamp> getSelectedCollection(Collection<? extends SimpleStamp> collection) {
        Collection<SimpleStamp> result = new ArrayList<>();
        try {
            for (SimpleStamp item : collection) {
                if (item.isSelected) {
                    result.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
