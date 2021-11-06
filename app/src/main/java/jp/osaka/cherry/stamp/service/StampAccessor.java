package jp.osaka.cherry.stamp.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import jp.osaka.cherry.stamp.utils.controller.BaseCommand;
import jp.osaka.cherry.stamp.utils.controller.IWorker;
import jp.osaka.cherry.stamp.utils.controller.command.Backup;
import jp.osaka.cherry.stamp.utils.controller.command.Restore;

import static jp.osaka.cherry.stamp.constants.STAMP.EXTRA_STAMP_LIST;

/**
 * パーソンアクセス
 */
class StampAccessor implements IWorker {

    /**
     * @serial メッセージの定義(RESTORE)
     */
    private static final int MSG_RESTORE = 0;

    /**
     * @serial メッセージの定義(BACKUP)
     */
    private static final int MSG_BACKUP = 1;

    /**
     * @serial モデル
     */
    private final StampDatabase mDatabase;

    /**
     * @serial コールバック
     */
    private final Callbacks mCallbacks;

    /**
     * @serial インスタンス
     */
    private final StampAccessor mSelf;

    /**
     * @serial スレッド
     */
    private final HandlerThread mThread;

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler;

    /**
     * コンストラクタ
     */
    StampAccessor(Context context, Callbacks callbacks) {

        mCallbacks = callbacks;

        mDatabase = new StampDatabase(context);

        mSelf = this;

        mThread = new HandlerThread("StampList");
        mThread.start();

        mHandler =  new Handler(mThread.getLooper()) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void handleMessage(@NonNull Message msg) {

                // 処理開始を通知
                if (mCallbacks != null) {
                    mCallbacks.onStarted(mSelf, (BaseCommand) msg.obj);
                }

                switch(msg.what) {
                    case MSG_RESTORE: {
                        Restore command = (Restore) msg.obj;
                        mDatabase.restore();
                        StampList status = new StampList(mDatabase.getStamp());
                        // 処理結果を通知
                        if (mCallbacks != null) {
                            mCallbacks.onUpdated(mSelf, command, status);
                        }
                        if (mCallbacks != null) {
                            mCallbacks.onSuccessed(mSelf, (BaseCommand) msg.obj);
                        }
                        break;
                    }
                    case MSG_BACKUP: {
                        Backup command = (Backup) msg.obj;
                        ArrayList<SimpleStamp> collection = command.args.getParcelableArrayList(EXTRA_STAMP_LIST);
                        mDatabase.backup(collection);
                        if (mCallbacks != null) {
                            mCallbacks.onSuccessed(mSelf, (BaseCommand) msg.obj);
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BaseCommand command) {
        if (command instanceof Restore) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_RESTORE);
                Message msg = mHandler.obtainMessage(MSG_RESTORE, command);
                mHandler.sendMessage(msg);
            }
        }
        if (command instanceof Backup) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_BACKUP);
                Message msg = mHandler.obtainMessage(MSG_BACKUP, command);
                mHandler.sendMessage(msg);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_RESTORE);
            mHandler.removeMessages(MSG_BACKUP);
        }
        if (mThread != null && mThread.isAlive()) {
            mThread.quit();
        }
    }
}
