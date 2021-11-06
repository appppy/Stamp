package jp.osaka.cherry.stamp.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.osaka.cherry.stamp.R;
import jp.osaka.cherry.stamp.SimpleState;
import jp.osaka.cherry.stamp.android.view.ICollectionView;
import jp.osaka.cherry.stamp.constants.COLOR;
import jp.osaka.cherry.stamp.databinding.ActivityTrashBinding;
import jp.osaka.cherry.stamp.service.SimpleStamp;
import jp.osaka.cherry.stamp.service.StampClient;
import jp.osaka.cherry.stamp.utils.StampHelper;

import static jp.osaka.cherry.stamp.constants.ACTION.INSERT;
import static jp.osaka.cherry.stamp.constants.ACTION.MODIFY;
import static jp.osaka.cherry.stamp.constants.COLOR.BLUE;
import static jp.osaka.cherry.stamp.constants.COLOR.RED;
import static jp.osaka.cherry.stamp.constants.COLOR.WHITE;
import static jp.osaka.cherry.stamp.constants.COLOR.YELLOW;
import static jp.osaka.cherry.stamp.constants.DISPLAY.EMPTY;
import static jp.osaka.cherry.stamp.constants.DISPLAY.LINEAR;
import static jp.osaka.cherry.stamp.constants.SELECTION.MULTI_SELECTED;
import static jp.osaka.cherry.stamp.constants.SELECTION.SELECTED;
import static jp.osaka.cherry.stamp.constants.SELECTION.SELECTED_ALL;
import static jp.osaka.cherry.stamp.constants.SELECTION.SELECTING;
import static jp.osaka.cherry.stamp.constants.SELECTION.UNSELECTED;
import static jp.osaka.cherry.stamp.constants.STAMP.EXTRA_CONTENT;
import static jp.osaka.cherry.stamp.utils.ActivityTransitionHelper.getStartActivity;
import static jp.osaka.cherry.stamp.utils.SelectHelper.getSelectedCollection;
import static jp.osaka.cherry.stamp.utils.StampHelper.copyStampList;
import static jp.osaka.cherry.stamp.utils.StampHelper.getStamp;


/**
 * メインアクティビティ
 */
public class TrashActivity extends AppCompatActivity implements
        StampClient.Callbacks,
        DrawerLayout.DrawerListener,
        NavigationView.OnNavigationItemSelectedListener,
        ICollectionView.Callbacks<SimpleStamp>,
        View.OnClickListener,
        SimpleState.Callbacks {

    /**
     * @serial 状態
     */
    public SimpleState mState = new SimpleState();

    /**
     * @serial プリファレンス
     */
    private SharedPreferences mPref;

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler();

    /**
     * @serial クライアント
     */
    private final StampClient mClient = new StampClient(this, this);

    /**
     * @serial データセット
     */
    private ArrayList<SimpleStamp> mDataSet = new ArrayList<>();

    /**
     * @serial バックアップ
     */
    private final ArrayList<SimpleStamp> mBackup = new ArrayList<>();

    /**
     * @serial 選択データ
     */
    private final ArrayList<SimpleStamp> mSelected = new ArrayList<>();

    /**
     * @serial 進む
     */
    private final ArrayList<Action> mRedos = new ArrayList<>();

    /**
     * @serial 戻る
     */
    private final ArrayList<Action> mUndos = new ArrayList<>();

    /**
     * @serial バインディング
     */
    private ActivityTrashBinding mBinding;

    /**
     * @serial トグル
     */
    private ActionBarDrawerToggle mToggle;

    /**
     * @serial ビュー
     */
    private ICollectionView<SimpleStamp> mCollectionView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdated(Object object, final List<SimpleStamp> people) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    boolean result;
                    result = mDataSet.isEmpty() || mDataSet.size() != people.size() || !mDataSet.equals(people);
                    if (result) {
                        // レジューム状態の確認
                        if (mState.isResumed()) {
                            return;
                        }
                        // データ設定
                        mDataSet.clear();
                        mDataSet.addAll(people);
                        // データ更新
                        updateView(toList(mDataSet));
                    }
                    // 次の動作を指定
                    for (Action doAction : mRedos) {
                        if (doAction.action.equals(MODIFY)) {
                            if (!doAction.object.isTrash) {
                                UnTrashRunner runner = new UnTrashRunner(doAction.object);
                                mHandler.post(runner);
                            } else {
                                SimpleStamp dest = getStamp(doAction.object.uuid, mDataSet);
                                if (!dest.equals(doAction.object)) {
                                    if (StampHelper.isModified(dest, doAction.object)) {
                                        ChangeRunner runner = new ChangeRunner(doAction.object);
                                        mHandler.post(runner);
                                    }
                                }
                            }
                        }
                    }
                    mRedos.clear();

                    // 選択状態を解除
                    mHandler.post(new DiselectRunner());
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View view) {
        mHandler.post(new DiselectRunner());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSelectChanged(SimpleState state) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // 選択状態の取得
                    switch (mState.getSelection()) {
                        case UNSELECTED: {
                            // コレクションビューが空の場合
                            if (mCollectionView != null) {
                                // アクションバーの設定
                                ActionBar bar = getSupportActionBar();
                                if (bar != null) {
                                    bar.setDisplayHomeAsUpEnabled(false);
                                }

                                // 選択の解除
                                mCollectionView.diselect();

                                // ナビゲーションドローワーの設定
                                enableNavigationDrawer();

                                // ツールバーの変更
                                updateTitle(toList(mDataSet));

                                // メニューを設定
                                getFragmentManager().invalidateOptionsMenu();
                            }
                            break;
                        }
                        // 何もしない
                        case SELECTED_ALL: {
                            // コレクションビューが空の場合
                            if (mCollectionView != null) {

                                // ナビゲーションの設定解除
                                disableNavigationDrawer();

                                // アクションバーの設定
                                ActionBar bar = getSupportActionBar();
                                if (bar != null) {
                                    bar.setDisplayHomeAsUpEnabled(true);
                                }

                                // 全選択
                                mSelected.clear();
                                mSelected.addAll(mCollectionView.selectedAll());

                                // ツールバーの設定
                                mBinding.toolbar.setTitle(String.valueOf(mSelected.size()));

                                // メニュー更新
                                getFragmentManager().invalidateOptionsMenu();
                            }
                            break;
                        }
                        case MULTI_SELECTED:
                        case SELECTED: {
                            // ナビゲーションの設定解除
                            disableNavigationDrawer();

                            // アクションバーの設定
                            ActionBar bar = getSupportActionBar();
                            if (bar != null) {
                                bar.setDisplayHomeAsUpEnabled(true);
                            }

                            // ツールバーの設定
                            mBinding.toolbar.setTitle(String.valueOf(mSelected.size()));

                            // メニュー更新
                            getFragmentManager().invalidateOptionsMenu();
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisplayChanged(SimpleState state) {
        // 処理なし
    }

    /**
     * アイテムのポップアップメニュー選択
     *
     * @param collectionView コレクションビュー
     * @param view           アイテムビュー
     * @param item           アイテム
     */
    @Override
    public void onSelectedMore(ICollectionView<SimpleStamp> collectionView, final View view, final SimpleStamp item) {
        // We need to post a Runnable to show the file_selected_one to make sure that the PopupMenu is
        // correctly positioned. The reason being that the view may change position before the
        // PopupMenu is shown.
        view.post(new Runnable() {
            @Override
            public void run() {
                showPopupMenu(view, item);
            }
        });
    }


    // BEGIN_INCLUDE(show_popup)
    private void showPopupMenu(final View view, final SimpleStamp item) {

        // Create a PopupMenu, giving it the clicked view for an anchor
        final PopupMenu popup = new PopupMenu(this, view);

        // Inflate our menu resource into the PopupMenu's Menu
        popup.getMenuInflater().inflate(R.menu.trash_selected_more, popup.getMenu());

        // Set a listener so we are notified if a menu item is clicked
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                // アイテムを選択する
                mSelected.clear();
                mSelected.add(item);

                // アイテムのメニューを選択する
                onOptionsItemSelected(menuItem);

                return false;
            }
        });

        // Finally show the PopupMenu
        popup.show();
    }
    // END_INCLUDE(show_popup)

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSelected(ICollectionView<SimpleStamp> collectionView, View view, SimpleStamp item) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSelectedChanged(ICollectionView<SimpleStamp> collectionView, View view, SimpleStamp item, Collection<? extends SimpleStamp> collection) {
        // 選択数の変更
        mSelected.clear();
        mSelected.addAll(getSelectedCollection(collection));

        // 選択状態の確認
        if (mSelected.size() == collection.size()) {
            // 全選択
            if (!mState.changeSelection(SELECTED_ALL)) {
                // 遷移なしの場合
                // 選択数を変更
                mBinding.toolbar.setTitle(String.valueOf(mSelected.size()));
                // メニュー更新
                getFragmentManager().invalidateOptionsMenu();
            }
        } else if (mSelected.size() > 1) {
            // マルチ選択
            if (!mState.changeSelection(MULTI_SELECTED)) {
                // 遷移なしの場合
                // 選択数を変更
                mBinding.toolbar.setTitle(String.valueOf(mSelected.size()));
                // メニュー更新
                getFragmentManager().invalidateOptionsMenu();
            }
        } else if (mSelected.size() == 1) {
            // 選択
            if (!mState.changeSelection(SELECTED)) {
                // 遷移なしの場合
                // 選択数を変更
                mBinding.toolbar.setTitle(String.valueOf(mSelected.size()));
                // メニュー更新
                getFragmentManager().invalidateOptionsMenu();
            }
        } else  {
            // 非選択
            mState.changeSelection(UNSELECTED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSwiped(ICollectionView<SimpleStamp> collectionView, SimpleStamp item) {
        // バックアップ
        mBackup.clear();
        copyStampList(mBackup, mDataSet);

        // データ設定
        item.isTrash = false;
        for (SimpleStamp dest : mDataSet) {
            // 一致
            if (dest.equal(item)) {
                dest.copyStamp(item);
            }
        }

        // プログレスの終了
        updateProgressBar();
        // コレクションビュー更新
        if (mCollectionView != null) {
            mUndos.clear();
            // ビューの更新
            int position = mCollectionView.remove(item);
            // 戻す処理に追加
            mUndos.add(new Action(INSERT, position, getStamp(item.uuid, mBackup)));
            Collections.reverse(mUndos);
        }

        // 設定
        mClient.setStamp(mDataSet);

        // スナックバーの生成
        String message = getString(R.string.restored_item);
        makeUndoSnackbar(mBinding.coordinatorLayout, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScroll(ICollectionView<SimpleStamp> view) {
        // 処理なし
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScrollFinished(ICollectionView<SimpleStamp> view) {
        // 処理なし
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMoveChanged(ICollectionView<SimpleStamp> view, Collection<? extends SimpleStamp> collection) {

        // キャスト
        ArrayList<SimpleStamp> arrayList = new ArrayList<>(collection);

        // 未選択
        mState.changeSelection(SELECTING);

        // アクションバーの設定
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(false);
        }
        // ツールバーの変更
        updateTitle(arrayList);

        // ナビゲーションドローワーの設定
        enableNavigationDrawer();

        // メニューを設定
        getFragmentManager().invalidateOptionsMenu();

        // データ変更
        for (SimpleStamp src1 : mDataSet) {
            boolean isChecked = true;
            for (SimpleStamp src2 : arrayList) {
                if (src1.equal(src2)) {
                    isChecked = false;
                }
            }
            if (isChecked) {
                arrayList.add(src1);
            }
        }
        mDataSet.clear();
        copyStampList(mDataSet, arrayList);

        // 保存
        mClient.setStamp(mDataSet);
    }

    /**
     * 更新通知
     *
     * @param view       ビュー
     * @param collection コレクション
     */
    @Override
    public void onUpdated(ICollectionView<SimpleStamp> view, Collection<? extends SimpleStamp> collection) {
        // キャスト
        ArrayList<SimpleStamp> arrayList = new ArrayList<>(collection);
        // 空ビューの更新
        updateEmptyView(arrayList);
        // タイトル更新
        updateTitle(arrayList);
        // メニューの更新
        updateMenu(arrayList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自身の取得

        // プリファレンスの設定
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        // レイアウトの設定
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_trash);

        setSupportActionBar(mBinding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.trash);
        }
        // ナビゲーションの設定
        enableNavigationDrawer();

        // 登録
        mState.setId(R.id.trash);
        mState.registerCallacks(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        // レジューム状態の設定
        mState.setResumed(true);

        // サービスの接続
        mClient.connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {

        // レジューム状態の設定
        mState.setResumed(false);

        // サービスの非接続
        mClient.disconnect();

        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        // 解除
        mState.unregisterCallacks();
        super.onDestroy();
    }

    /**
     * ナビゲーションドローワ―の有効化
     */
    private void enableNavigationDrawer() {
        // ナビゲーションドローワ―の生成
        if (mToggle == null) {
            mToggle = new ActionBarDrawerToggle(
                    this,
                    mBinding.drawerLayout,
                    mBinding.toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
        }
        // ナビゲーションドローワ―の設定
        mToggle.setDrawerIndicatorEnabled(true);
        mToggle.setToolbarNavigationClickListener(this);
        mToggle.syncState();
        mBinding.drawerLayout.addDrawerListener(mToggle);
        mBinding.drawerLayout.addDrawerListener(this);
        mBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mBinding.navView.setNavigationItemSelectedListener(this);
    }

    /**
     * ナビゲーションドローワ―の無効化
     */
    private void disableNavigationDrawer() {
        // ナビゲーションドローワ―の設定解除
        if (mToggle != null) {
            mToggle.setDrawerIndicatorEnabled(false);
            mToggle.syncState();
            mBinding.drawerLayout.removeDrawerListener(mToggle);
            mBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // 選択状態の取得
            switch (mState.getSelection()) {
                case SELECTED_ALL:
                case MULTI_SELECTED:
                case SELECTING:
                case SELECTED: {
                    // 選択解除
                    mHandler.post(new DiselectRunner());
                    break;
                }
                default: {
                    super.onBackPressed();
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // メニュー設定
        // 選択状態の確認
        switch (mState.getSelection()) {
            case SELECTING:
            case UNSELECTED: {
                if (mState.getDisplay() == EMPTY) {
                    getMenuInflater().inflate(R.menu.trash_empty, menu);
                } else {
                    getMenuInflater().inflate(R.menu.trash, menu);
                }
                break;
            }
            case SELECTED:
            case MULTI_SELECTED: {
                getMenuInflater().inflate(R.menu.trash_selected, menu);
                break;
            }
            case SELECTED_ALL: {
                getMenuInflater().inflate(R.menu.trash_selected_all, menu);
                break;
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // レジューム状態の確認
        if(mState.isResumed()) {
            return super.onOptionsItemSelected(item);
        }

        int id = item.getItemId();

        // 識別子ごとの処理
        switch (id) {
            // リスト表示
            case R.id.menu_empty: {

                // バックアップ
                mBackup.clear();
                copyStampList(mBackup, mDataSet);

                mDataSet = toEmptyList(mDataSet);

                // プログレスの終了
                updateProgressBar();
                // コレクションビュー更新
                if (mCollectionView != null) {
                    mCollectionView.changeAll(toList(mDataSet));
                }

                // 設定
                mClient.setStamp(mDataSet);

                // メッセージ保持
                String message = getString(R.string.empty_trash_is_done);
                Snackbar.make(mBinding.coordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // レジューム状態の確認
                                if(mState.isResumed()) {
                                    return;
                                }
                                // データ設定
                                mDataSet.clear();
                                copyStampList(mDataSet, mBackup);
                                // ビュー更新
                                updateView(toList(mDataSet));
                                // 設定
                                mClient.setStamp(mDataSet);
                            }
                        })
                        .show();

                return true;
            }
            case R.id.menu_untrash: {
                mHandler.post(new SelectedUnTrashRunner());
                return true;
            }
            case R.id.menu_selected_all: {
                mHandler.post(new SelectedAllRunner());
                return true;
            }
            case R.id.menu_white: {
                mHandler.post(new SelectedChangeColorRunner(WHITE));
                return true;
            }
            case R.id.menu_red: {
                mHandler.post(new SelectedChangeColorRunner(RED));
                return true;
            }
            case R.id.menu_blue: {
                mHandler.post(new SelectedChangeColorRunner(BLUE));
                return true;
            }
            case R.id.menu_yellow: {
                mHandler.post(new SelectedChangeColorRunner(YELLOW));
                return true;
            }
            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ビューの更新
     */
    private void updateView(ArrayList<SimpleStamp> collection) {
        // プログレスの終了
        updateProgressBar();
        // 空ビューの更新
        updateEmptyView(collection);
        // メニューの更新
        updateMenu(collection);
        // コレクションビュー更新
        updateCollectionView(collection);
        // タイトル更新
        updateTitle(collection);
    }

    /**
     * プログレスバーの更新
     */
    private void updateProgressBar() {
        // プログレスバーの終了
        mBinding.productImageLoading.setVisibility(View.INVISIBLE);
    }

    /**
     * 空ビューの更新
     */
    private void updateEmptyView(ArrayList<SimpleStamp> collection) {
        // 空ビューの更新
        boolean isEmpty = collection.isEmpty();
        if (isEmpty) {
            // 空の場合
            mBinding.emptyView.setVisibility(View.VISIBLE);
        } else {
            // 空でない場合
            mBinding.emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * メニューの更新
     *
     * @param collection コレクション
     */
    private void updateMenu(ArrayList<SimpleStamp> collection) {
        boolean isEmpty = collection.isEmpty();
        if (isEmpty) {
            mState.changeDisplay(EMPTY);
        } else {
            mState.changeDisplay(LINEAR);
        }
        getFragmentManager().invalidateOptionsMenu();
    }

    /**
     * コレクションビューの更新
     */
    private void updateCollectionView(ArrayList<SimpleStamp> collection) {
        mCollectionView = ListFragment.newInstance(collection);
        // ビューの取得
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, (Fragment) mCollectionView)
                .commit();
    }

    /**
     * タイトルの更新
     *
     * @param collection コレクション
     */
    private void updateTitle(ArrayList<SimpleStamp> collection) {
        StringBuilder sb = new StringBuilder();
        if (collection.isEmpty()) {
            sb.append(this.getString(R.string.trash));
        } else {
            sb.append(this.getString(R.string.trash)).append("  ").append(collection.size());
        }
        mBinding.toolbar.setTitle(sb.toString());
        sb.delete(0, sb.length());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        // 処理なし
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        // 処理なし
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        if (mState.getId() != R.id.trash) {
            Intent intent = getIntent();
            intent.setClass(getApplicationContext(), getStartActivity(mState.getId()));
            startActivity(intent);
            overridePendingTransition(R.animator.fade_out, R.animator.fade_in);
            finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerStateChanged(int newState) {
        // 処理なし
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mState.setId(item.getItemId());
        // コンテンツ識別子の保存
        mPref.edit().putInt(EXTRA_CONTENT, item.getItemId()).apply();
        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    /**
     * スナックバーの生成
     *
     * @param layout  レイアウト
     * @param message メッセージ
     */
    private void makeUndoSnackbar(CoordinatorLayout layout, String message) {
        Snackbar.make(layout, message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), new View.OnClickListener() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void onClick(View v) {
                        // レジューム状態の確認
                        if(mState.isResumed()) {
                            return;
                        }

                        // プログレスバーの更新
                        updateProgressBar();
                        // コレクションビューが空でない場合
                        if (mCollectionView != null) {
                            // コレクションビュー更新
                            try {
                                for (Action undo : mUndos) {
                                    switch (undo.action) {
                                        case INSERT: {
                                            // コレクションビューの更新
                                            mCollectionView.insert(undo.arg, undo.object);
                                            break;
                                        }
                                        case CHANGE: {
                                            // コレクションビューの更新
                                            mCollectionView.change(undo.object);
                                            break;
                                        }
                                        case REMOVE: {
                                            // コレクションビューの更新
                                            mCollectionView.remove(undo.object);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                // コレクションビュー更新
                                updateCollectionView(mBackup);
                            }
                            mUndos.clear();
                        }

                        ArrayList<SimpleStamp> list = toList(mBackup);
                        // 空ビューの更新
                        updateEmptyView(list);
                        // タイトル更新
                        updateTitle(list);
                        // メニューの更新
                        updateMenu(list);

                        // データの設定
                        mDataSet.clear();
                        copyStampList(mDataSet, mBackup);

                        // 設定
                        mClient.setStamp(mDataSet);
                    }
                })
                .show();
    }

    /**
     * 選択解除
     */
    private class DiselectRunner implements Runnable {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // 選択解除
                mState.changeSelection(UNSELECTED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ゴミ箱の解除
     */
    private class UnTrashRunner implements Runnable {

        /**
         * @serial アイテム
         */
        SimpleStamp mItem;

        /**
         * コンストラクタ
         *
         * @param item アイテム
         */
        UnTrashRunner(SimpleStamp item) {
            mItem = item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                // バックアップ
                mBackup.clear();
                copyStampList(mBackup, mDataSet);

                // データの設定
                for (SimpleStamp dest : mDataSet) {
                    // 一致
                    if (dest.equal(mItem)) {
                        // データの変更
                        dest.copyStamp(mItem);
                    }
                }
                // プログレスの終了
                updateProgressBar();
                // コレクションビュー更新
                if (mCollectionView != null) {
                    mUndos.clear();
                    // コレクションビューの更新
                    int position = mCollectionView.remove(getStamp(mItem.uuid, mDataSet));
                    // 戻す処理に追加
                    mUndos.add(new Action(INSERT, position, getStamp(mItem.uuid, mBackup)));
                    Collections.reverse(mUndos);
                }

                // 設定
                mClient.setStamp(mDataSet);

                // スナックバーの生成
                String message = getString(R.string.restored_item);
                makeUndoSnackbar(mBinding.coordinatorLayout, message);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 選択のゴミ箱の解除
     */
    private class SelectedUnTrashRunner implements Runnable {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                // 選択解除
                mState.changeSelection(UNSELECTED);

                // バックアップ
                mBackup.clear();
                copyStampList(mBackup, mDataSet);

                // データの設定
                for (SimpleStamp src : mSelected) {
                    // 選択状態の解除
                    src.isSelected = false;
                    // ゴミ箱状態の解除
                    src.isTrash = false;
                }
                for (SimpleStamp dest : mDataSet) {
                    for (SimpleStamp src : mSelected) {
                        // 一致
                        if (dest.equal(src)) {
                            // データの変更
                            dest.copyStamp(src);
                        }
                    }
                }
                // プログレスの終了
                updateProgressBar();
                // コレクションビュー更新
                // コレクションビューの更新が空でない場合
                if (mCollectionView != null) {
                    mUndos.clear();
                    for (SimpleStamp src : mSelected) {
                        int position;
                        if (!src.isTrash) {
                            // コレクションビューの更新
                            position = mCollectionView.remove(src);
                            // 戻す処理に追加
                            mUndos.add(new Action(INSERT, position, getStamp(src.uuid, mBackup)));
                        }
                    }
                    Collections.reverse(mUndos);
                }

                // 設定
                mClient.setStamp(mDataSet);

                // 選択アイテムの削除
                mSelected.clear();

                // スナックバーの生成
                String message;
                if (mUndos.size() == 1) {
                    message = getString(R.string.restored_item);
                    makeUndoSnackbar(mBinding.coordinatorLayout, message);
                } else if (mUndos.size() > 1) {
                    message = getString(R.string.restored_some_items, mUndos.size());
                    makeUndoSnackbar(mBinding.coordinatorLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 全選択
     */
    private class SelectedAllRunner implements Runnable {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // 全選択
                mState.changeSelection(SELECTED_ALL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 選択したアーカイブの色変更
     */
    private class SelectedChangeColorRunner implements Runnable {

        COLOR mCOLOR;

        /**
         * コンストラクタ
         *
         * @param color 色
         */
        SelectedChangeColorRunner(COLOR color) {
            mCOLOR = color;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                // 選択状態の解除
                mState.changeSelection(UNSELECTED);

                // データの設定
                for (SimpleStamp src : mSelected) {
                    // 選択状態の解除
                    src.isSelected = false;
                    // 色の設定
                    src.color = mCOLOR;
                }
                for (SimpleStamp dest : mDataSet) {
                    for (SimpleStamp src : mSelected) {
                        // 一致
                        if (dest.equal(src)) {
                            // データの変更
                            dest.copyStamp(src);
                        }
                    }
                }

                // プログレスバーの更新
                updateProgressBar();
                // コレクションビュー更新
                if (mCollectionView != null) {
                    //mUndos.clear();
                    for (SimpleStamp src : mSelected) {
                        // ビューを更新
                        mCollectionView.change(src);
                    }
                }

                // 設定
                mClient.setStamp(mDataSet);

                // 選択アイテムの削除
                mSelected.clear();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 変更
     */
    private class ChangeRunner implements Runnable {

        /**
         * @serial アイテム
         */
        SimpleStamp mItem;

        /**
         * コンストラクタ
         *
         * @param item アイテム
         */
        ChangeRunner(SimpleStamp item) {
            mItem = item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                // コレクションビューが空でなく
                // アイテムが空でない場合
                if ((mCollectionView != null) && (mItem != null)) {

                    // 設定
                    for (SimpleStamp dest : mDataSet) {
                        // 一致
                        if (dest.equal(mItem)) {
                            dest.copyStamp(mItem);
                        }
                    }

                    // プログレスバーの更新
                    updateProgressBar();
                    // コレクションビュー更新
                    mCollectionView.change(getStamp(mItem.uuid, mDataSet));

                    // 設定
                    mClient.setStamp(mDataSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ゴミ箱のコンテンツのリスト取得
     *
     * @param collection コレクション
     * @return ゴミ箱のコンテンツのリスト
     */
    public static ArrayList<SimpleStamp> toList(Collection<SimpleStamp> collection) {
        ArrayList<SimpleStamp> result = new ArrayList<>();
        for (SimpleStamp item : collection) {
            if (item.isTrash) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * ゴミ箱を空にする
     *
     * @param collection コレクション
     * @return ゴミ箱を空にしたリスト
     */
    static ArrayList<SimpleStamp> toEmptyList(Collection<SimpleStamp> collection) {
        ArrayList<SimpleStamp> result = new ArrayList<>();
        for (SimpleStamp item : collection) {
            if (!item.isTrash) {
                result.add(item);
            }
        }
        return result;
    }

}
