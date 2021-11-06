
package jp.osaka.cherry.stamp.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

import jp.osaka.cherry.stamp.android.IClient;
import jp.osaka.cherry.stamp.constants.INTERFACE;


/**
 * ピープルクライアント
 */
public class StampClient implements IClient {

    /**
     * @serial コンテキスト
     */
    private final Context mContext;

    /**
     * @serial コールバック
     */
    private static Callbacks mCallbacks;

    /**
     * @serial インタフェース
     */
    private IStampService mBinder;

    /**
     * @serial 自身
     */
    private final StampClient mSelf;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public StampClient(Context context, Callbacks callbacks) {
        mSelf = this;
        mContext = context;
        mCallbacks = callbacks;
    }

    /** コネクション */
    private final ServiceConnection mConnection = new ServiceConnection() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = IStampService.Stub.asInterface(service);
            try {
                mBinder.registerCallback(mCallback);
                mBinder.getStamp();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
            mContext.unbindService(mConnection);
        }
    };

    /**
     * @serial コールバック
     */
    private final IStampServiceCallback mCallback = new IStampServiceCallback.Stub() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void update(List<SimpleStamp> stamp) {
            if (mCallbacks != null) {
                mCallbacks.onUpdated(mSelf, stamp);
            }
        }
    };

    /**
     * 接続
     */
    public void connect() {
        try {
            Intent intent = new Intent(INTERFACE.IStampService);
            intent.setPackage("jp.osaka.cherry.stamp");
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }  catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解除
     */
    public void disconnect() {
        if (mBinder != null) {
            try {
                mBinder.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (mContext != null) {
            try {
                mContext.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ピープルの設定
     *
     * @param stamp ピープル
     */
    public void setStamp(List<SimpleStamp> stamp) {
        if (mBinder != null) {
            try {
                mBinder.setStamp(stamp);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * コールバックインタフェース
     */
    public interface Callbacks {
        /**
         * コマンド更新通知
         *
         * @param object オブジェクト
         * @param people ピープル
         */
        void onUpdated(Object object, List<SimpleStamp> people);
    }
}
