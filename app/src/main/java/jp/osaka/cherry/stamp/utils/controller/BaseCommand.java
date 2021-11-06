package jp.osaka.cherry.stamp.utils.controller;

import android.os.Bundle;

/**
 * コマンドクラスの基底クラス
 */
public abstract class BaseCommand {

    /**
     * @serial 引数
     */
    public Bundle args = new Bundle();
}
