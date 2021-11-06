package jp.osaka.cherry.stamp.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.Collection;

import jp.osaka.cherry.stamp.R;
import jp.osaka.cherry.stamp.android.view.DividerItemDecoration;
import jp.osaka.cherry.stamp.android.view.ICollectionView;
import jp.osaka.cherry.stamp.android.view.adapter.ItemListener;
import jp.osaka.cherry.stamp.constants.STAMP;
import jp.osaka.cherry.stamp.databinding.FragmentBinding;
import jp.osaka.cherry.stamp.service.SimpleStamp;
import jp.osaka.cherry.stamp.utils.timer.SimpleTimer;
import jp.osaka.cherry.stamp.utils.timer.TimerListener;

import static jp.osaka.cherry.stamp.constants.STAMP.TIMEOUT_FLOATING_ACTION_BUTTON_HIDE;
import static jp.osaka.cherry.stamp.utils.SelectHelper.isMultiSelected;
import static jp.osaka.cherry.stamp.utils.SelectHelper.isSelected;
import static jp.osaka.cherry.stamp.utils.ThemeHelper.getImageResource;
import static jp.osaka.cherry.stamp.utils.ThemeHelper.getLineColor;
import static jp.osaka.cherry.stamp.utils.ThemeHelper.getSelectedLineColor;
import static jp.osaka.cherry.stamp.utils.timer.TimerHelper.createTimer;
import static jp.osaka.cherry.stamp.utils.timer.TimerHelper.startTimer;
import static jp.osaka.cherry.stamp.utils.timer.TimerHelper.stopTimer;

/**
 * リストフラグメント
 */
public class ListFragment extends Fragment implements
        ICollectionView<SimpleStamp>,
        ItemListener<SimpleStamp>,
        TimerListener {

    /**
     * @serial コールバック
     */
    private Callbacks<SimpleStamp> mCallbacks;

    /**
     * @serial フローティングアクションボタン表示タイマー
     */
    private SimpleTimer mTimer;

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler();

    /**
     * @serial 自身
     */
    private final ListFragment mSelf;

    /**
     * @serial アダプタ
     */
    private ListAdapter mAdapter;

    /**
     * @serial バインディング
     */
    private FragmentBinding mBinding;

    /**
     * インスタンス取得
     *
     * @param collection カード
     */
    public static ListFragment newInstance(ArrayList<SimpleStamp> collection) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(STAMP.EXTRA_STAMP_LIST, collection);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * コンストラクタ
     */
    public ListFragment() {
        mSelf = this;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = ((Callbacks<SimpleStamp>) getActivity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // タイマ生成
        mTimer = createTimer(mTimer, TIMEOUT_FLOATING_ACTION_BUTTON_HIDE, this);
        // 再生成を抑止
        setRetainInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        // タイマ停止
        stopTimer(mTimer);
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBinding = DataBindingUtil.bind(requireView());

        ArrayList<SimpleStamp> people = requireArguments().getParcelableArrayList(STAMP.EXTRA_STAMP_LIST);

        // アダプタの設定
        mAdapter = new ListAdapter(getContext(), this, people);

        // レイアウトの設定
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mBinding.collection.addItemDecoration(new DividerItemDecoration(requireActivity()));
        mBinding.collection.setLayoutManager(layoutManager);
        mBinding.collection.setAdapter(mAdapter);
        mBinding.collection.setItemAnimator(new DefaultItemAnimator());
        mBinding.collection.setVerticalScrollBarEnabled(false);
        mBinding.collection.addOnScrollListener(new RecyclerView.OnScrollListener() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                try {
                    // タイマ開始
                    startTimer(mTimer);
                    if (mCallbacks != null) {
                        mCallbacks.onScroll(mSelf);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            /**
             * {@inheritDoc}
             */
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // 複数選択の場合、ドラッグ無効、スワイプ無効
                if (isMultiSelected(mAdapter.getCollection())) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, 0) |
                            makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, 0);
                }
                // 単数選択の場合、
                if (isSelected(mAdapter.getCollection())) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END) |
                            makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, 0);
                }

                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP) |
                        makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                try {
                    int from = viewHolder.getAdapterPosition();
                    int to = target.getAdapterPosition();
                    if (from >= 0 && to >= 0) {

                        // 選択状態を解除
                        SimpleStamp person = mAdapter.getCollection().get(from);
                        person.isSelected = false;

                        mAdapter.move(from, to);
                        mCallbacks.onMoveChanged(mSelf, mAdapter.getCollection());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                try {
                    switch (actionState) {
                        case ItemTouchHelper.ACTION_STATE_IDLE:
                        case ItemTouchHelper.ACTION_STATE_SWIPE: {
                            break;
                        }
                        default: {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                float elevation = 8 * getResources().getDisplayMetrics().density;
                                viewHolder.itemView.setElevation(elevation);
                            }

                            ListAdapter.BindingHolder holder = (ListAdapter.BindingHolder) viewHolder;
                            int position = viewHolder.getAdapterPosition();
                            SimpleStamp item = mAdapter.getCollection().get(position);

                            // アイコンの設定
                            holder.getBinding().icon.setImageResource(R.drawable.ic_check_circle_grey_600_24dp);
                            // 背景の設定
                            holder.getBinding().cardView.setBackgroundColor(getSelectedLineColor(getContext(), item.color));
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        viewHolder.itemView.setElevation(0);
                    }

                    ListAdapter.BindingHolder holder = (ListAdapter.BindingHolder) viewHolder;

                    // アイテムの選択状態の取得
                    int position = viewHolder.getAdapterPosition();
                    if (position >= 0) {
                        SimpleStamp item = mAdapter.getCollection().get(position);
                        // アイテムが選択状態でなければ、選択表示を解除
                        if (!item.isSelected) {
                            // アイコンの設定
                            holder.getBinding().icon.setImageResource(getImageResource(getContext(), item.color));
                            // 背景の設定
                            holder.getBinding().cardView.setBackgroundColor(getLineColor(getContext(), item.color));
                            // コールバック実行
                            mCallbacks.onSelectedChanged(mSelf, holder.getBinding().cardView, item, mAdapter.getCollection());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                try {
                    int position = viewHolder.getAdapterPosition();
                    if (position >= 0) {
                        if (mCallbacks != null) {
                            mCallbacks.onSwiped(mSelf, mAdapter.getCollection().get(position));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.6f;
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mBinding.collection);
        mAdapter.setItemTouchHelper(itemTouchHelper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTimer(Object timer, int timeOutCount, boolean timerState) {
        mHandler.post(new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                if (null != mCallbacks) {
                    mCallbacks.onScrollFinished(mSelf);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void diselect() {
        if (mAdapter != null) {
            Collection<SimpleStamp> collection = mAdapter.getCollection();
            for (SimpleStamp item : collection) {
                item.isSelected = false;
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends SimpleStamp> selectedAll() {
        Collection<? extends SimpleStamp> result = null;
        if (mAdapter != null) {
            Collection<SimpleStamp> collection = mAdapter.getCollection();
            for (SimpleStamp item : collection) {
                item.isSelected = true;
            }
            mAdapter.notifyDataSetChanged();
            result = mAdapter.getCollection();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(int index, SimpleStamp item) {
        if (mAdapter != null) {
            mAdapter.insert(index, item);
            if (index == 0) {
                mBinding.collection.scrollToPosition(index);
            }
            if (mCallbacks != null) {
                mCallbacks.onUpdated(this, mAdapter.getCollection());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(@NonNull SimpleStamp item) {
        if (mAdapter != null) {
            mAdapter.insert(0, item);
            mBinding.collection.scrollToPosition(0);
            if (mCallbacks != null) {
                mCallbacks.onUpdated(this, mAdapter.getCollection());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int remove(@NonNull SimpleStamp item) {
        int location = 0;
        if (mAdapter != null) {
            location = mAdapter.getCollection().indexOf(item);
            if (location >= 0) {
                mAdapter.remove(location);
            }
            if (mCallbacks != null) {
                mCallbacks.onUpdated(this, mAdapter.getCollection());
            }
        }
        return location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int change(@NonNull SimpleStamp item) {
        int position = 0;
        if (mAdapter != null) {
            position = mAdapter.set(item);
            if (mCallbacks != null) {
                mCallbacks.onUpdated(this, mAdapter.getCollection());
            }
        }
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeAll(Collection<? extends SimpleStamp> collection) {
        if (mAdapter != null) {
            mAdapter.setAll(collection);
            if (mCallbacks != null) {
                mCallbacks.onUpdated(this, mAdapter.getCollection());
            }
        }
    }

    /**
     * クリック
     *
     * @param view ビュー
     * @param item アイテム
     */
    @Override
    public void onClickMore(View view, SimpleStamp item) {
        if (mCallbacks != null) {
            mCallbacks.onSelectedMore(this, view, item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View view, SimpleStamp item) {
        if (mCallbacks != null) {
            mCallbacks.onSelected(this, view, item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLongClick(View view, SimpleStamp item) {
        if (mCallbacks != null) {
            mCallbacks.onSelectedChanged(this, view, item, mAdapter.getCollection());
        }
    }
}