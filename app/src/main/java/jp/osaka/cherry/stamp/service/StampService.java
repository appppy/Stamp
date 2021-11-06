
package jp.osaka.cherry.stamp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.osaka.cherry.stamp.constants.STAMP;
import jp.osaka.cherry.stamp.utils.controller.BaseCommand;
import jp.osaka.cherry.stamp.utils.controller.Controller;
import jp.osaka.cherry.stamp.utils.controller.IWorker;
import jp.osaka.cherry.stamp.utils.controller.command.Backup;
import jp.osaka.cherry.stamp.utils.controller.command.Restore;
import jp.osaka.cherry.stamp.utils.controller.status.BaseStatus;

/**
 * ピープルサービス
 */
public class StampService extends Service implements
        IWorker.Callbacks {

    /**
     * @serial コールバックリスト
     */
    private final RemoteCallbackList<IStampServiceCallback> mCallbacks =
            new RemoteCallbackList<>();

    /**
     * @serial コントローラー
     */
    private final Controller mController = new Controller();

    /**
     * @serial インタフェース
     */
    private final IStampService.Stub mBinder = new IStampService.Stub() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void registerCallback(IStampServiceCallback callback) {
            mCallbacks.register(callback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void unregisterCallback(IStampServiceCallback callback) {
            mCallbacks.unregister(callback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setStamp(List<SimpleStamp> stamp) {
            // バックアップ
            BaseCommand command = new Backup();
            ArrayList<SimpleStamp> arrayList = new ArrayList<>(stamp);
            command.args.putParcelableArrayList(STAMP.EXTRA_STAMP_LIST, arrayList);
            mController.start(command);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getStamp() {
            // リストア
            mController.start(new Restore());
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 登録
        mController.register(new StampAccessor(this, this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        // コールバックの削除
        mCallbacks.kill();

        // 停止
        mController.stop();

        // 解除
        mController.unregisterAll();

        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        IBinder result = null;
        if (IStampService.class.getName().equals(intent.getAction())) {
            result = mBinder;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStarted(IWorker worker, BaseCommand command) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdated(IWorker worker, BaseCommand command, BaseStatus status) {
        try {
            // データベースコントローラー
            if (worker instanceof StampAccessor) {
                // コマンド
                if (command instanceof Restore) {
                    // リストア
                    if (status instanceof StampList) {
                        StampList s = (StampList) status;
                        // ブロードキャスト
                        broadcast(s.collection);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 成功通知
     *
     * @param worker  ワーカー
     * @param command コマンド
     */
    @Override
    public void onSuccessed(IWorker worker, BaseCommand command) {
    }

    /**
     * ブロードキャスト
     *
     * @param collection コレクション
     */
    public void broadcast(Collection<SimpleStamp> collection) {
        List<SimpleStamp> list = new ArrayList<>(collection);
        int n = mCallbacks.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                mCallbacks.getBroadcastItem(i).update(list);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
    }
}
