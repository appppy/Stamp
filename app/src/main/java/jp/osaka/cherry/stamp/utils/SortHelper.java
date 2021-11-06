package jp.osaka.cherry.stamp.utils;


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.osaka.cherry.stamp.service.SimpleStamp;

/**
 * 入れ替えヘルパ
 *
 */
public class SortHelper {

    /**
     * 作成日でソートしたコレクションの取得
     *
     * @param collection コレクション
     * @return 作成日でソートしたコレクション
     */
    public static Collection<SimpleStamp> toSortByDateCreatedCollection(Collection<SimpleStamp> collection) {
        try {
            Collections.sort((List<SimpleStamp>) collection, new Comparator<SimpleStamp>() {
                @Override
                public int compare(SimpleStamp lhs, SimpleStamp rhs) {
                    return (int) (lhs.creationDate - rhs.creationDate);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collection;
    }

}
