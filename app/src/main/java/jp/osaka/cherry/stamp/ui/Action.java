package jp.osaka.cherry.stamp.ui;


import jp.osaka.cherry.stamp.constants.ACTION;
import jp.osaka.cherry.stamp.service.SimpleStamp;

/**
 * 実行
 */
public class Action {

    /**
     * 識別子
     */
    public ACTION action;

    /**
     * パラメータ
     */
    public int arg;

    /**
     * オブジェクト
     */
    public SimpleStamp object;

    /**
     * コンストラクタ
     *
     * @param action 識別子
     * @param position パラメータ
     * @param object オブジェクト
     */
    public Action(ACTION action, int position, SimpleStamp object) {
        this.action = action;
        this.arg = position;
        this.object = object;
    }
}
