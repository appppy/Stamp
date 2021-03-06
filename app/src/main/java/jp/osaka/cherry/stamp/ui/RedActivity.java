package jp.osaka.cherry.stamp.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import jp.osaka.cherry.stamp.databinding.ActivityRedBinding;
import jp.osaka.cherry.stamp.service.SimpleStamp;
import jp.osaka.cherry.stamp.service.StampClient;
import jp.osaka.cherry.stamp.utils.StampHelper;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static jp.osaka.cherry.stamp.constants.ACTION.CHANGE;
import static jp.osaka.cherry.stamp.constants.ACTION.CREATE;
import static jp.osaka.cherry.stamp.constants.ACTION.INSERT;
import static jp.osaka.cherry.stamp.constants.ACTION.MODIFY;
import static jp.osaka.cherry.stamp.constants.ACTION.REMOVE;
import static jp.osaka.cherry.stamp.constants.ActivityTransition.REQUEST_CREATE_ITEM;
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
import static jp.osaka.cherry.stamp.constants.STAMP.EXTRA_SIMPLE_STAMP;
import static jp.osaka.cherry.stamp.constants.STAMP.EXTRA_SORT_ID;
import static jp.osaka.cherry.stamp.constants.STAMP.SORT_BY_DATE_CREATED;
import static jp.osaka.cherry.stamp.utils.ActivityTransitionHelper.getStartActivity;
import static jp.osaka.cherry.stamp.utils.SelectHelper.getSelectedCollection;
import static jp.osaka.cherry.stamp.utils.SortHelper.toSortByDateCreatedCollection;
import static jp.osaka.cherry.stamp.utils.StampHelper.copyStampList;
import static jp.osaka.cherry.stamp.utils.StampHelper.getStamp;
import static jp.osaka.cherry.stamp.utils.StampHelper.isMax;


/**
 * ??????????????????????????????
 */
public class RedActivity extends AppCompatActivity implements
        StampClient.Callbacks,
        ICollectionView.Callbacks<SimpleStamp>,
        DrawerLayout.DrawerListener,
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        SimpleState.Callbacks {
    /**
     * @serial ?????????????????????
     */
    private SharedPreferences mPref;

    /**
     * @serial ??????
     */
    private final SimpleState mState = new SimpleState();

    /**
     * @serial ??????????????????
     */
    private final ArrayList<SimpleStamp> mDataSet = new ArrayList<>();

    /**
     * @serial ??????????????????
     */
    private final ArrayList<SimpleStamp> mBackup = new ArrayList<>();

    /**
     * @serial ????????????????????????
     */
    private final ArrayList<SimpleStamp> mSelected = new ArrayList<>();

    /**
     * @serial ??????????????????
     */
    private final StampClient mClient = new StampClient(this, this);

    /**
     * @serial ????????????
     */
    private final Handler mHandler = new Handler();

    /**
     * @serial ??????
     */
    private final ArrayList<Action> mUndos = new ArrayList<>();

    /**
     * @serial ??????
     */
    private final ArrayList<Action> mRedos = new ArrayList<>();

    /**
     * @serial ???????????????????????????
     */
    private ICollectionView<SimpleStamp> mCollectionView;

    /**
     * @serial ?????????
     */
    private ActionBarDrawerToggle mToggle;

    /**
     * @serial ???????????????
     */
    private Toolbar mToolbar;

    /**
     * @serial ????????????????????????
     */
    private DrawerLayout mDrawerLayout;

    /**
     * @serial ??????????????????????????????
     */
    private NavigationView mNavigationView;

    /**
     * @serial ????????????????????????????????????
     */
    private CoordinatorLayout mCoordinatorLayout;

    /**
     * @serial ?????????????????????
     */
    private ProgressBar mProgressBar;

    /**
     * @serial ????????????
     */
    private ImageView mEmptyView;

    /**
     * @serial floating action button
     */
    private FloatingActionButton mFab;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ???????????????

        // ??????????????????????????????
        mPref = getDefaultSharedPreferences(this);

        // ?????????????????????
        ActivityRedBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_red);
        mToolbar = binding.toolbar;
        mDrawerLayout = binding.drawerLayout;
        mNavigationView = binding.navView;
        mCoordinatorLayout = binding.coordinatorLayout;
        mProgressBar = binding.productImageLoading;
        mEmptyView = binding.emptyView;
        mFab = binding.fab;

        // ????????????????????????
        setSupportActionBar(mToolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.red_stamp);
        }

        // ??????????????????????????????????????????????????????
        setupFloatingActionButton();

        // ??????????????????????????????
        enableNavigationDrawer();

        // ??????
        mState.setId(R.id.red);
        mState.registerCallacks(this);

        if(!mRedos.isEmpty()) {
            mPref.edit().putInt(EXTRA_CONTENT, R.id.red).apply();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        // ??????????????????????????????
        mState.setResumed(true);

        // ?????????????????????
        mClient.connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {

        // ??????????????????????????????
        mState.setResumed(false);

        // ????????????????????????
        mClient.disconnect();

        super.onPause();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {

        // ??????
        mState.unregisterCallacks();

        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ????????????
        if (requestCode == REQUEST_CREATE_ITEM) {
            if (resultCode == RESULT_OK) {
                // ??????????????????
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    SimpleStamp item = bundle.getParcelable(EXTRA_SIMPLE_STAMP);
                    // ????????????????????????????????????
                    mRedos.clear();
                    mRedos.add(new Action(CREATE, 0, item));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // ?????????????????????
            switch (mState.getSelection()) {
                case SELECTED_ALL:
                case MULTI_SELECTED:
                case SELECTING:
                case SELECTED: {
                    // ????????????
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
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (mState.getSelection()) {
            case SELECTING:
            case UNSELECTED: {
                switch (mState.getDisplay()) {
                    case EMPTY: {
                        getMenuInflater().inflate(R.menu.main_empty, menu);
                        break;
                    }
                    default:
                    case LINEAR: {
                        getMenuInflater().inflate(R.menu.main_linear, menu);
                        break;
                    }
                }
                break;
            }
            case SELECTED:
            case MULTI_SELECTED: {
                getMenuInflater().inflate(R.menu.main_selected, menu);
                break;
            }
            case SELECTED_ALL: {
                getMenuInflater().inflate(R.menu.main_selected_all, menu);
                break;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // ??????????????????????????????
        if(mState.isResumed()) {
            return super.onOptionsItemSelected(item);
        }

        int id = item.getItemId();

        // ????????????????????????
        switch (id) {
            case R.id.menu_by_date_created: {
                mPref.edit().putInt(EXTRA_SORT_ID, SORT_BY_DATE_CREATED).apply();
                ArrayList<SimpleStamp> collection = (ArrayList<SimpleStamp>) toSortByDateCreatedCollection(mDataSet);
                Collections.reverse(collection);
                updateView(toList(collection));
                mClient.setStamp(collection);
                break;
            }
            case R.id.menu_swap_vert: {
                Collections.reverse(mDataSet);
                updateView(toList(mDataSet));
                return true;
            }
            case R.id.menu_archive: {
                mHandler.post(new SelectedArchiveRunner());
                return true;
            }
            case R.id.menu_trash: {
                mHandler.post(new SelectedTrashRunner());
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
                        // ??????????????????????????????
                        if (mState.isResumed()) {
                            return;
                        }
                        // ???????????????
                        mDataSet.clear();
                        mDataSet.addAll(people);
                        // ???????????????
                        updateView(toList(mDataSet));
                        // FAB
                        setupFloatingActionButton();
                        mFab.show();
                    }

                    // ?????????????????????
                    for (Action doAction : mRedos) {
                        if (!isMax(mDataSet)) {
                            if (doAction.action.equals(CREATE)) {
                                CreateRunner runner = new CreateRunner(doAction.object);
                                mHandler.post(runner);
                            }
                        }
                        if (doAction.action.equals(MODIFY)) {
                            SimpleStamp dest = getStamp(doAction.object.uuid, mDataSet);
                            if (!dest.equals(doAction.object)) {
                                if (StampHelper.isModified(dest, doAction.object)) {
                                    ChangeRunner runner = new ChangeRunner(doAction.object);
                                    mHandler.post(runner);
                                } else {
                                    ModifyRunner runner = new ModifyRunner(doAction.object);
                                    mHandler.post(runner);
                                }
                            }
                        }
                    }
                    mRedos.clear();

                    // ?????????????????????
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        mState.setId(item.getItemId());

        // ?????????????????????????????????
        mPref.edit().putInt(EXTRA_CONTENT, item.getItemId()).apply();

        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        // ????????????
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        // ????????????
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

        if (mState.getId() != R.id.red) {
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
        // ????????????
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param collectionView ???????????????????????????
     * @param view           ?????????????????????
     * @param item           ????????????
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
        popup.getMenuInflater().inflate(R.menu.main_selected_all, popup.getMenu());

        // Set a listener so we are notified if a menu item is clicked
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // ?????????????????????
                mSelected.clear();
                mSelected.add(item);

                // ??????????????????
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

        // ??????????????????
        mSelected.clear();
        mSelected.addAll(getSelectedCollection(collection));

        // ?????????????????????
        if (mSelected.size() == collection.size()) {
            // ?????????
            if (!mState.changeSelection(SELECTED_ALL)) {
                // ?????????????????????
                // ??????????????????
                mToolbar.setTitle(String.valueOf(mSelected.size()));
                // ??????????????????
                getFragmentManager().invalidateOptionsMenu();
            }
        } else if (mSelected.size() > 1) {
            // ???????????????
            if (!mState.changeSelection(MULTI_SELECTED)) {
                // ?????????????????????
                // ??????????????????
                mToolbar.setTitle(String.valueOf(mSelected.size()));
                // ??????????????????
                getFragmentManager().invalidateOptionsMenu();
            }
        } else if (mSelected.size() == 1) {
            // ??????
            if (!mState.changeSelection(SELECTED)) {
                // ?????????????????????
                // ??????????????????
                mToolbar.setTitle(String.valueOf(mSelected.size()));
                // ??????????????????
                getFragmentManager().invalidateOptionsMenu();
            }
        } else {
            // ?????????
            mState.changeSelection(UNSELECTED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSwiped(ICollectionView<SimpleStamp> collectionView, SimpleStamp item) {

        // ??????????????????
        mBackup.clear();
        copyStampList(mBackup, mDataSet);

        // ???????????????
        item.isArchive = true;
        // ??????
        for (SimpleStamp dest : mDataSet) {
            // ??????
            if (dest.equal(item)) {
                dest.copyStamp(item);
            }
        }

        // ??????????????????????????????
        updateProgressBar();
        // ?????????????????????????????????
        if (mCollectionView != null) {
            mUndos.clear();
            // ????????????????????????????????????
            int position = mCollectionView.remove(item);
            mUndos.add(new Action(INSERT, position, getStamp(item.uuid, mBackup)));
            Collections.reverse(mUndos);
        }

        // ??????
        mClient.setStamp(mDataSet);

        // ???????????????????????????
        String message = getString(R.string.moved_to_archive_item);
        makeUndoSnackbar(mCoordinatorLayout, message);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onMoveChanged(ICollectionView<SimpleStamp> view, Collection<? extends SimpleStamp> collection) {

        // ????????????
        ArrayList<SimpleStamp> arrayList = new ArrayList<>(collection);

        mState.changeSelection(SELECTING);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(false);
        }

        updateTitle(arrayList);
//        mBinding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue_600));
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue_800));
//            //getWindow().setBackgroundDrawable(DevelopHelper.getBackgroundDrawble(mSelf, mState));
//        }
//        // ??????????????????
//        mBinding.mainContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue_800));
        enableNavigationDrawer();
        getFragmentManager().invalidateOptionsMenu();

        //???????????????
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

        // ??????
        mClient.setStamp(mDataSet);

    }

    /**
     * ????????????
     *
     * @param view       ?????????
     * @param collection ??????????????????
     */
    @Override
    public void onUpdated(ICollectionView<SimpleStamp> view, Collection<? extends SimpleStamp> collection) {
        // ????????????
        ArrayList<SimpleStamp> arrayList = new ArrayList<>(collection);
        // ?????????????????????
        updateEmptyView(arrayList);
        // ??????????????????
        updateTitle(arrayList);
        // ?????????????????????
        updateMenu(arrayList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScroll(ICollectionView<SimpleStamp> view) {
        mFab.hide();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScrollFinished(ICollectionView<SimpleStamp> view) {
        mFab.show();
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
                    // ?????????????????????
                    switch (mState.getSelection()) {
                        case UNSELECTED: {
                            // ??????????????????????????????
                            ActionBar bar = getSupportActionBar();
                            if (bar != null) {
                                bar.setDisplayHomeAsUpEnabled(false);
                            }

                            // ???????????????
                            if (mCollectionView != null) {
                                mCollectionView.diselect();
                            }

                            // ?????????????????????????????????????????????
                            enableNavigationDrawer();

                            // ????????????????????????
                            updateTitle(toList(mDataSet));
//                            mBinding.toolbar.setBackgroundColor(ContextCompat.getColor(mSelf, R.color.light_blue_600));
//                            // ??????????????????????????????
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                                getWindow().setStatusBarColor(ContextCompat.getColor(mSelf, R.color.light_blue_800));
//                            }
//
//                            // ??????????????????
//                            mBinding.mainContainer.setBackgroundColor(ContextCompat.getColor(mSelf, R.color.light_blue_800));

                            // ??????????????????????????????????????????????????????
                            setupFloatingActionButton();
                            mFab.show();

                            // ?????????????????????
                            getFragmentManager().invalidateOptionsMenu();

                            break;
                        }
                        // ???????????????
                        case SELECTED_ALL: {

                            // ????????????????????????????????????
                            disableNavigationDrawer();

                            // ??????????????????????????????
                            ActionBar bar = getSupportActionBar();
                            if (bar != null) {
                                bar.setDisplayHomeAsUpEnabled(true);
                            }

                            // ?????????
                            if (mCollectionView != null) {
                                mSelected.clear();
                                mSelected.addAll(mCollectionView.selectedAll());
                            }

                            // ????????????????????????
                            mToolbar.setTitle(String.valueOf(mSelected.size()));

                            // ?????????????????????????????????????????????????????????
                            //mBinding.fab.hide();
                            setupFloatingActionButton();

                            // ??????????????????
                            getFragmentManager().invalidateOptionsMenu();

                            break;
                        }

                        case MULTI_SELECTED:
                        case SELECTED: {

                            // ????????????????????????????????????
                            disableNavigationDrawer();

                            // ??????????????????????????????
                            ActionBar bar = getSupportActionBar();
                            if (bar != null) {
                                bar.setDisplayHomeAsUpEnabled(true);
                            }

                            // ????????????????????????
                            mToolbar.setTitle(String.valueOf(mSelected.size()));
//                            mBinding.toolbar.setBackgroundColor(ContextCompat.getColor(mSelf, R.color.blue_grey_600));
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                                getWindow().setStatusBarColor(ContextCompat.getColor(mSelf, R.color.blue_grey_800));
//                            }
//
//                            // ??????????????????
//                            mBinding.mainContainer.setBackgroundColor(ContextCompat.getColor(mSelf, R.color.blue_grey_800));

                            // ?????????????????????????????????????????????????????????
                            //mBinding.fab.hide();
                            setupFloatingActionButton();

                            // ??????????????????
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
        // ????????????
    }


    /**
     * ??????????????????????????????????????????????????????
     */
    private void setupFloatingActionButton() {

        if (mState.getSelection() == UNSELECTED) {
            //mBinding.fab.setImageResource(R.drawable.ic_add_white_36dp);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ??????????????????????????????
                    if (mState.isResumed()) {
                        return;
                    }

                    SimpleStamp stamp = SimpleStamp.createInstance();
                    stamp.color = RED;
                    CreateRunner runner = new CreateRunner(stamp);
                    mHandler.post(runner);
                }
            });
        } else {
            mFab.hide();
        }
    }

    /**
     * ??????????????????????????????
     */
    private void enableNavigationDrawer() {
        if (mToggle == null) {
            mToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    mToolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
        }
        mToggle.setDrawerIndicatorEnabled(true);
        mToggle.setToolbarNavigationClickListener(this);
        mToggle.syncState();
        mDrawerLayout.addDrawerListener(mToggle);
        mDrawerLayout.addDrawerListener(this);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * ??????????????????????????????
     */
    private void disableNavigationDrawer() {
        if (mToggle != null) {
            mToggle.setDrawerIndicatorEnabled(false);
            mToggle.syncState();
            mDrawerLayout.removeDrawerListener(mToggle);
        }
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * ??????????????????
     *
     * @param collection ??????????????????
     */
    private void updateView(ArrayList<SimpleStamp> collection) {
        // ????????????????????????
        updateProgressBar();
        // ?????????????????????
        updateEmptyView(collection);
        // ?????????????????????
        updateMenu(collection);
        // ?????????????????????????????????
        updateCollectionView(collection);
        // ??????????????????
        updateTitle(collection);
    }

    /**
     * ??????????????????????????????
     */
    private void updateProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * ?????????????????????
     *
     * @param collection ??????????????????
     */
    private void updateEmptyView(List<SimpleStamp> collection) {
        // ?????????????????????
        if (collection.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * ?????????????????????
     *
     * @param collection ??????????????????
     */
    private void updateMenu(List<SimpleStamp> collection) {
        if (collection.isEmpty()) {
            mState.changeDisplay(EMPTY);
        } else {
            mState.changeDisplay(LINEAR);
        }
        getFragmentManager().invalidateOptionsMenu();
    }

    /**
     * ????????????????????????????????????
     *
     * @param collection ??????????????????
     */
    private void updateCollectionView(ArrayList<SimpleStamp> collection) {
        // ??????????????????
        mCollectionView = getCollectionView(collection);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, (Fragment) mCollectionView)
                .commit();
    }

    /**
     * ?????????????????????
     *
     * @param collection ??????????????????
     */
    private void updateTitle(ArrayList<SimpleStamp> collection) {
        StringBuilder sb = new StringBuilder();
        if (collection.isEmpty()) {
            sb.append(this.getString(R.string.red_stamp));
        } else {
            sb.append(this.getString(R.string.red_stamp)).append("  ").append(collection.size());
        }
        mToolbar.setTitle(sb.toString());
        sb.delete(0, sb.length());
    }

    /**
     * ????????????????????????????????????
     *
     * @param collection ??????????????????
     * @return ???????????????????????????
     */
    private ICollectionView<SimpleStamp> getCollectionView(ArrayList<SimpleStamp> collection) {
        return ListFragment.newInstance(collection);
    }

    /**
     * ???????????????????????????
     *
     * @param layout  ???????????????
     * @param message ???????????????
     */
    private void makeUndoSnackbar(CoordinatorLayout layout, String message) {
        Snackbar.make(layout, message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), new View.OnClickListener() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void onClick(View v) {

                        // ??????????????????????????????
                        if(mState.isResumed()) {
                            return;
                        }

                        // ??????????????????????????????
                        updateProgressBar();
                        // ????????????????????????????????????????????????
                        if (mCollectionView != null) {
                            // ?????????????????????????????????
                            try {
                                for (Action undo : mUndos) {
                                    switch (undo.action) {
                                        case INSERT: {
                                            // ????????????????????????????????????
                                            mCollectionView.insert(undo.arg, undo.object);
                                            break;
                                        }
                                        case CHANGE: {
                                            // ????????????????????????????????????
                                            mCollectionView.change(undo.object);
                                            break;
                                        }
                                        case REMOVE: {
                                            // ????????????????????????????????????
                                            mCollectionView.remove(undo.object);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                // ?????????????????????????????????
                                updateCollectionView(mBackup);
                            }
                            mUndos.clear();
                        }

                        // ???????????????
                        ArrayList<SimpleStamp> list = toList(mBackup);
                        // ?????????????????????
                        updateEmptyView(list);
                        // ??????????????????
                        updateTitle(list);
                        // ?????????????????????
                        updateMenu(list);

                        // ???????????????
                        mDataSet.clear();
                        copyStampList(mDataSet, mBackup);

                        // ??????
                        mClient.setStamp(mDataSet);
                    }
                })
                .show();
    }

    /**
     * ??????
     */
    private class CreateRunner implements Runnable {

        /**
         * @serial ????????????
         */
        SimpleStamp mItem;

        /**
         * ?????????????????????
         *
         * @param item ????????????
         */
        CreateRunner(SimpleStamp item) {
            mItem = item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                // ??????????????????????????????????????????
                // ?????????????????????????????????
                if ((mCollectionView != null) && (mItem != null)) {

                    // ??????????????????
                    mBackup.clear();
                    copyStampList(mBackup, mDataSet);

                    // ?????????????????????
                    mDataSet.add(0, mItem);

                    // ??????????????????????????????
                    updateProgressBar();
                    // ?????????????????????????????????
                    mUndos.clear();
                    mCollectionView.insert(0, mItem);
                    mUndos.add(new Action(REMOVE, 0, mItem));
                        for (SimpleStamp dest : mDataSet) {

                                if (dest.creationDate == mItem.creationDate) {
                                    int position = mCollectionView.change(getStamp(mItem.uuid, mDataSet));
                                    mUndos.add(new Action(CHANGE, position, getStamp(mItem.uuid, mBackup)));

                                }

                        }
                    Collections.reverse(mUndos);

                    // ??????
                    mClient.setStamp(mDataSet);

                    // ???????????????????????????
                    makeUndoSnackbar(mCoordinatorLayout, getString(R.string.created_item));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????
     */
    private class ModifyRunner implements Runnable {

        /**
         * @serial ????????????
         */
        SimpleStamp mItem;

        /**
         * ?????????????????????
         *
         * @param item ????????????
         */
        ModifyRunner(SimpleStamp item) {
            mItem = item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                // ??????????????????????????????????????????
                // ?????????????????????????????????
                if ((mCollectionView != null) && (mItem != null)) {

                    // ??????????????????
                    mBackup.clear();
                    copyStampList(mBackup, mDataSet);

                    // ??????
                    for (SimpleStamp dest : mDataSet) {
                        // ??????
                        if (dest.equal(mItem)) {
                            dest.copyStamp(mItem);
                        }
                    }

                    // ??????????????????????????????
                    updateProgressBar();
                    // ?????????????????????????????????
                    mUndos.clear();
                    if (mItem.isArchive) {
                        // ????????????????????????????????????
                        int position = mCollectionView.remove(getStamp(mItem.uuid, mDataSet));
                        mUndos.add(new Action(INSERT, position, getStamp(mItem.uuid, mBackup)));
                    } else if (mItem.isTrash) {
                        // ????????????????????????????????????
                        int position = mCollectionView.remove(getStamp(mItem.uuid, mDataSet));
                        mUndos.add(new Action(INSERT, position, getStamp(mItem.uuid, mBackup)));
                    } else {
                        // ????????????????????????????????????
                        int position = mCollectionView.change(getStamp(mItem.uuid, mDataSet));
                        mUndos.add(new Action(CHANGE, position, getStamp(mItem.uuid, mBackup)));
                    }
                    for (SimpleStamp dest : mDataSet) {

                            if (dest.creationDate == mItem.creationDate) {

                                    if (!dest.isTrash && !dest.isArchive) {
                                        // ????????????????????????????????????
                                        int position = mCollectionView.change(dest);
                                        mUndos.add(new Action(CHANGE, position, getStamp(dest.uuid, mBackup)));
                                    }

                            }

                    }
                    Collections.reverse(mUndos);

                    // ??????
                    mClient.setStamp(mDataSet);

                    // ???????????????????????????
                    String message;
                    if (mItem.isArchive) {
                        message = getString(R.string.moved_to_archive_item);
                    } else if (mItem.isTrash) {
                        message = getString(R.string.moved_to_trash_item);
                    } else {
                        message = getString(R.string.modified_item);
                    }
                    makeUndoSnackbar(mCoordinatorLayout, message);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????????????????????
     */
    private class SelectedArchiveRunner implements Runnable {

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                // ????????????????????????????????????????????????
                if (mCollectionView != null) {

                    mState.changeSelection(UNSELECTED);

                    // ??????????????????
                    mBackup.clear();
                    copyStampList(mBackup, mDataSet);

                    for (SimpleStamp src : mSelected) {
                        src.isSelected = false;
                        src.isArchive = true;
                    }
                    for (SimpleStamp dest : mDataSet) {
                        for (SimpleStamp src : mSelected) {
                            // ??????
                            if (dest.equal(src)) {
                                dest.copyStamp(src);
                            }
                        }
                    }

                    // ??????????????????????????????
                    updateProgressBar();
                    // ?????????????????????????????????
                    mUndos.clear();
                    for (SimpleStamp src : mSelected) {
                        int position;
                        if (src.isArchive) {
                            // ????????????????????????????????????
                            position = mCollectionView.remove(src);
                            mUndos.add(new Action(INSERT, position, getStamp(src.uuid, mBackup)));
                        } else if (src.isTrash) {
                            // ????????????????????????????????????
                            position = mCollectionView.remove(src);
                            mUndos.add(new Action(INSERT, position, getStamp(src.uuid, mBackup)));
                        } else {
                            // ????????????????????????????????????
                            position = mCollectionView.change(src);
                            mUndos.add(new Action(CHANGE, position, getStamp(src.uuid, mBackup)));
                        }
                    }
                    Collections.reverse(mUndos);

                    // ??????
                    mClient.setStamp(mDataSet);

                    mSelected.clear();

                    String message;
                    if (mUndos.size() == 1) {
                        message = getString(R.string.moved_to_archive_item);
                        makeUndoSnackbar(mCoordinatorLayout, message);
                    } else if (mUndos.size() > 1) {
                        message = getString(R.string.moved_to_archive_some_items, mUndos.size());
                        makeUndoSnackbar(mCoordinatorLayout, message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ???????????????
     */
    private class SelectedTrashRunner implements Runnable {

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            try {
                mState.changeSelection(UNSELECTED);

                // ??????????????????
                mBackup.clear();
                copyStampList(mBackup, mDataSet);

                // ???????????????
                for (SimpleStamp src : mSelected) {
                    src.isSelected = false;
                    src.isTrash = true;
                }
                for (SimpleStamp dest : mDataSet) {
                    for (SimpleStamp src : mSelected) {
                        // ??????
                        if (dest.equal(src)) {
                            dest.copyStamp(src);
                        }
                    }
                }

                // ??????????????????????????????
                updateProgressBar();
                // ?????????????????????????????????
                mUndos.clear();
                if (mCollectionView != null) {
                    for (SimpleStamp src : mSelected) {
                        int position;
                        if (src != null) {
                            if (src.isArchive) {
                                // ????????????????????????????????????
                                position = mCollectionView.remove(src);
                                mUndos.add(new Action(INSERT, position, getStamp(src.uuid, mBackup)));
                            } else if (src.isTrash) {
                                // ????????????????????????????????????
                                position = mCollectionView.remove(src);
                                mUndos.add(new Action(INSERT, position, getStamp(src.uuid, mBackup)));
                            } else {
                                // ????????????????????????????????????
                                position = mCollectionView.change(src);
                                mUndos.add(new Action(CHANGE, position, getStamp(src.uuid, mBackup)));
                            }
                        }
                    }
                }
                Collections.reverse(mUndos);

                // ??????
                mClient.setStamp(mDataSet);

                // ????????????????????????
                mSelected.clear();

                String message;
                if (mUndos.size() == 1) {
                    message = getString(R.string.moved_to_trash_item);
                    makeUndoSnackbar(mCoordinatorLayout, message);
                } else if (mUndos.size() > 1) {
                    message = getString(R.string.moved_to_trash_some_items, mUndos.size());
                    makeUndoSnackbar(mCoordinatorLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????????
     */
    private class DiselectRunner implements Runnable {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // ????????????
                mState.changeSelection(UNSELECTED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????????
     */
    private class SelectedAllRunner implements Runnable {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // ?????????
                mState.changeSelection(SELECTED_ALL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private class SelectedChangeColorRunner implements Runnable {

        COLOR mCOLOR;

        /**
         * ?????????????????????
         *
         * @param color ???
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
                // ?????????????????????
                mState.changeSelection(UNSELECTED);

                // ??????????????????
                for (SimpleStamp src : mSelected) {
                    // ?????????????????????
                    src.isSelected = false;
                    // ????????????
                    src.color = mCOLOR;

                }
                for (SimpleStamp dest : mDataSet) {
                    for (SimpleStamp src : mSelected) {
                        // ??????
                        if (dest.equal(src)) {
                            // ??????????????????
                            dest.copyStamp(src);
                        }
                    }
                }

                // ??????????????????????????????
                updateProgressBar();
                // ?????????????????????????????????
                if (mCollectionView != null) {
                    //mUndos.clear();
                    for (SimpleStamp src : mSelected) {
                        // ??????????????????
                        mCollectionView.change(src);
                    }
                }

                // ??????
                mClient.setStamp(mDataSet);

                // ???????????????????????????
                mSelected.clear();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????
     */
    private class ChangeRunner implements Runnable {

        /**
         * @serial ????????????
         */
        SimpleStamp mItem;

        /**
         * ?????????????????????
         *
         * @param item ????????????
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
                // ??????????????????????????????????????????
                // ?????????????????????????????????
                if ((mCollectionView != null) && (mItem != null)) {

                    // ??????
                    for (SimpleStamp dest : mDataSet) {
                        // ??????
                        if (dest.equal(mItem)) {
                            dest.copyStamp(mItem);
                        }
                    }

                    // ??????????????????????????????
                    updateProgressBar();
                    // ?????????????????????????????????
                    mCollectionView.change(getStamp(mItem.uuid, mDataSet));

                    // ??????
                    mClient.setStamp(mDataSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param collection ??????????????????
     * @return ???????????????????????????????????????
     */
    public static ArrayList<SimpleStamp> toList(Collection<SimpleStamp> collection) {
        ArrayList<SimpleStamp> result = new ArrayList<>();
        for (SimpleStamp item : collection) {
            if (!item.isArchive && !item.isTrash) {
                result.add(item);
            }
        }
        return result;
    }

}
