/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.osaka.cherry.stamp.constants;

/**
 * 定数
 */
public final class STAMP {
    /**
     * @serial パッケージ名
     */
    public static final String PACKAGE = "jp.osaka.cherry.stamp";

    /**
     * @serial 最大数
     */
    public static final int MAX = 100;

    /**
     * @serial フローティングアクションボタン非表示タイムアウト
     */
    public static final int TIMEOUT_FLOATING_ACTION_BUTTON_HIDE = 1000;

    /*
     * 不定値
     */

    /**
     * @serial LONG不定値
     */
    public static final long INVALID_LONG_VALUE = -999L;

    /**
     * @serial STRING不定値
     */
    public static final String INVALID_STRING_VALUE = "";

    /*
     * インテントアクション
     */

    /*
     * カテゴリ
     */

    public static final String EXTRA_KEY =
            PACKAGE + ".EXTRA_KEY";

    /*
     * エキストラ
     */

    /**
     * @serial UUID
     */
    public static final String EXTRA_UUID =
            PACKAGE + ".EXTRA_UUID";

    /**
     * @serial 作成日
      */
    public static final String EXTRA_CREATION_DATE =
            PACKAGE + ".EXTRA_CREATION_DATE";

    /**
     * @serial 変更日
     */
    public static final String EXTRA_MODIFIED_DATE =
            PACKAGE + ".EXTRA_MODIFIED_DATE";

    /**
     * @serial 表示名
     */
    public static final String EXTRA_DISPLAY_NAME =
            PACKAGE + ".EXTRA_DISPLAY_NAME";

    /**
     * @serial ノート
     */
    public static final String EXTRA_NOTE =
            PACKAGE + ".EXTRA_NOTE";

    /**
     * @serial アーカイブの有無
     */
    public static final String EXTRA_IS_ARCHIVE =
            PACKAGE + ".EXTRA_IS_ARCHIVE";

    /**
     * @serial ゴミ箱の有無
     */
    public static final String EXTRA_IS_TRASH =
            PACKAGE + ".EXTRA_IS_TRASH";

    /**
     * @serial シンプルパーソン
     */
    public static final String EXTRA_SIMPLE_STAMP =
            PACKAGE + ".EXTRA_SIMMPLE_PERSON";

    /**
     * @serial シンプルピープル
     */
    public static final String EXTRA_STAMP_LIST =
            PACKAGE + ".EXTRA_STAMP_LIST";

    /**
     * @serial ソート
     */
    public static final String EXTRA_SORT_ID =
            PACKAGE + ".EXTRA_SORT_ID";

    /**
     * @serial 色
     */
    public static final String EXTRA_COLOR =
            PACKAGE + ".EXTRA_COLOR";

    /**
     * @serial タイムスタンプ
     */
    public static final String EXTRA_TIMESTAMP =
            PACKAGE + ".EXTRA_TIMESTAMP";


    public static final String EXTRA_COLOR_OF_STAMP =
            PACKAGE + ".EXTRA_COLOR_OF_STAMP";

    /**
     * ソート
     */
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_DATE_CREATED = SORT_BY_NAME + 1;

    /**
     * @serial コンテンツ
     */
    public static final String EXTRA_CONTENT =
            PACKAGE + ".EXTRA_CONTENT";
}
