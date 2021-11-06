package jp.osaka.cherry.stamp.utils.controller;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 制御インタフェース
 */
public class Controller implements IWorker {

    /**
     * 制御コレクション
     */
    private final Collection<IWorker> mCollection = new ArrayList<>();

    /**
     * 登録
     *
     * @param worker ワーカー
     */
    public void register(IWorker worker) {
        mCollection.add(worker);
    }

    /**
     * 解除
     */
    public void unregisterAll() {
        mCollection.clear();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BaseCommand command) {
        for(IWorker worker : mCollection) {
            worker.start(command);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        for(IWorker worker : mCollection) {
            worker.stop();
        }
    }
}
