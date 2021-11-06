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

package jp.osaka.cherry.stamp.service;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import jp.osaka.cherry.stamp.constants.COLOR;


/**
 * アイテム
 */
public class SimpleStamp implements Parcelable {

    /**
     * @serial 生成
     */
    public static final Creator<SimpleStamp> CREATOR =
            new Creator<SimpleStamp>() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public SimpleStamp createFromParcel(Parcel source) {
                    return new SimpleStamp(source);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public SimpleStamp[] newArray(int size) {
                    return new SimpleStamp[size];
                }
            };

    /**
     * @serial 識別子
     */
    public String id;

    /**
     * @serial ユニーク識別子
     */
    public String uuid;

    /**
     * @serial 作成日
     */
    public long creationDate;

    /**
     * @serial 変更日
     */
    public long modifiedDate;

    /**
     * @serial アーカイブの有無
     */
    public boolean isArchive;

    /**
     * @serial ゴミ箱の有無
     */
    public boolean isTrash;

    /**
     * @serial 色
     */
    public COLOR color;

    /**
     * @serial 選択
     */
    public boolean isSelected = false;

    /**
     * 人の生成
     *
     * @param id           識別子
     * @param uuid         ユニーク識別子
     * @param creationDate 作成日
     * @param modifiedDate 変更日
     * @param isArchive    アーカイブの有無
     * @param isTrash      ゴミ箱の有無
     * @param color        色
     */
    public SimpleStamp(
            String id,
            String uuid,
            long creationDate,
            long modifiedDate,
            boolean isArchive,
            boolean isTrash,
            COLOR color
    ) {
        this.id = id;
        this.uuid = uuid;
        this.creationDate = creationDate;
        this.modifiedDate = modifiedDate;
        this.isArchive = isArchive;
        this.isTrash = isTrash;
        this.color = color;
    }

    /**
     * コンストラクタ
     *
     * @param parcel パーシャル
     */
    public SimpleStamp(Parcel parcel) {
        id = parcel.readString();
        uuid = parcel.readString();
        creationDate = parcel.readLong();
        modifiedDate = parcel.readLong();
        isArchive = parcel.readByte() != 0;
        isTrash = parcel.readByte() != 0;
        color = COLOR.valueOf(parcel.readString());
    }

    /**
     * インスタンス生成
     *
     * @return インスタンス
     */
    public static SimpleStamp createInstance() {
        return new SimpleStamp(
                String.valueOf(UUID.randomUUID()),
                String.valueOf(UUID.randomUUID()),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                false,
                false,
                COLOR.WHITE
        );
    }

    /***
     * アイテムのコピー
     *
     * @param item アイテム
     */
    public void copyStamp(SimpleStamp item) {
        id = item.id;
        uuid = item.uuid;
        creationDate = item.creationDate;
        modifiedDate = item.modifiedDate;
        isArchive = item.isArchive;
        isTrash = item.isTrash;
        color = item.color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uuid);
        dest.writeLong(creationDate);
        dest.writeLong(modifiedDate);
        dest.writeByte((byte) (isArchive ? 1 : 0));
        dest.writeByte((byte) (isTrash ? 1 : 0));
        dest.writeString(color.name());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equal(Object o) {
        boolean result = false;
        if (o instanceof SimpleStamp) {
            SimpleStamp item = (SimpleStamp) o;
            if (item.uuid.equals(uuid)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * コンストラクタ
     *
     * @param object jsonオブジェクト
     */
    public SimpleStamp(JSONObject object) {
        try {
            id = object.getString("id");
            uuid = object.getString("uuid");
            creationDate = object.getLong("creationDate");
            modifiedDate = object.getLong("modifiedDate");
            isArchive = object.getBoolean("isArchive");
            isTrash = object.getBoolean("isTrash");
            color = COLOR.valueOf(object.getString("color"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
