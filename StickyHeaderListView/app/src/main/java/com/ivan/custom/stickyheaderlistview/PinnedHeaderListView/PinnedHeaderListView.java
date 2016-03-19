package com.ivan.custom.stickyheaderlistview.PinnedHeaderListView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 该ListView的header view会浮动在顶部
 * Created by Ivan on 16/2/13.
 */
public class PinnedHeaderListView extends ListView {

    /**
     * 浮动的section view
     */
    private SectionView sectionView;
    /**
     * 浮动的section view的底部阴影
     */
    private GradientDrawable sectionViewShadow;
    /**
     * 阴影高度
     */
    private int shadowHeight;
    /**
     * 画布要移动的距离
     */
    private int distanceY = 0;
    /**
     * 数据源适配器，要实现section浮动效果需要实现该适配器
     */
    private PinnedHeaderListAdapter pinnedHeaderListAdapter;
    /**
     * 由于该类已经实现了一个OnScrollListener，因此如果外部使用该类时要设置Onscroll回调的话，为了避免覆盖，实际实现的是这个回调
     */
    private OnScrollListener onScrollListener;

    private Context context;

    public PinnedHeaderListView(Context context) {
        super(context);
        initView(context);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (onScrollListener != null) {
                    onScrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (onScrollListener != null) {
                    onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }

                if (pinnedHeaderListAdapter == null || pinnedHeaderListAdapter.getCount() == 0) {
                    return;
                }
                createPinnedSection(firstVisibleItem);
                if (sectionView == null) {
                    return;
                }
                int nextSectionViewTop = findNextSectionViewTop(firstVisibleItem, firstVisibleItem + 1, firstVisibleItem + visibleItemCount);
                if (nextSectionViewTop != -1) {
                    int currentSectionViewBottom = sectionView.view.getBottom();
                    distanceY = nextSectionViewTop - currentSectionViewBottom;
                    if (distanceY > 0) {
                        distanceY = 0;
                    }
                } else {
                    distanceY = 0;
                }
            }
        });
    }

    /**
     * 计算[from,to)范围内的下一个section view相对其parent view的顶部位置
     * @param firstVisibleItem 当前ListView第一个可见的item的位置
     * @param from 计算的起始item的位置
     * @param to 计算的结束item的位置
     * @return 若计算范围内的item中存在section view，则返回其相对parent view的top值，否则返回-1
     */
    private int findNextSectionViewTop(int firstVisibleItem, int from, int to) {
        if (pinnedHeaderListAdapter == null) {
            return -1;
        }
        for (int i=from;i<to;i++) {
            if (pinnedHeaderListAdapter.isSectionView(i)) {
                View view = getChildAt(i - firstVisibleItem);
                return view.getTop();
            }
        }
        return -1;
    }

    /**
     * 创建一个浮动在顶部的section view
     * @param firstVisibleItem 当前ListView可见的第一个item的position
     */
    private void createPinnedSection(int firstVisibleItem) {
        final int firstViewSection = pinnedHeaderListAdapter.sectionOfItem(firstVisibleItem);
        // 第一个可见的item不属于任何section
        if (firstViewSection == -1) {
            sectionView = null;
            destroySectionShadow();
            return;
        }
        // 第一个完全可见的item就是一个section，不需要阴影和浮动，反之创建阴影
        if (firstViewSection == firstVisibleItem && getChildAt(0).getTop() == 0) {
            sectionView = null;
            destroySectionShadow();
            return;
        } else {
            createSectionShadow();
        }
        // 第一个可见的item属于当前section，不需要重新创建浮动section
        if (sectionView != null && firstViewSection == sectionView.section) {
            return;
        }
        if (sectionView == null) {
            sectionView = new SectionView();
        }
        View firstVisibleView = pinnedHeaderListAdapter.getView(firstViewSection,sectionView.view,PinnedHeaderListView.this);
        LayoutParams layoutParams = (LayoutParams) firstVisibleView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = (LayoutParams) generateDefaultLayoutParams();
            firstVisibleView.setLayoutParams(layoutParams);
        }

        int heightMode = MeasureSpec.getMode(layoutParams.height);
        int heightSize = MeasureSpec.getSize(layoutParams.height);

        if (heightMode == MeasureSpec.UNSPECIFIED) heightMode = MeasureSpec.EXACTLY;

        int maxHeight = getHeight() - getListPaddingTop() - getListPaddingBottom();
        if (heightSize > maxHeight) heightSize = maxHeight;

        // measure & layout
        int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
        int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        firstVisibleView.measure(ws, hs);
        firstVisibleView.layout(0, 0, firstVisibleView.getMeasuredWidth(), firstVisibleView.getMeasuredHeight());

        sectionView.view = firstVisibleView;
        sectionView.section = firstViewSection;
    }

    /**
     * 创建渐变阴影
     */
    private void createSectionShadow() {
        if (sectionViewShadow == null) {
            sectionViewShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] { Color.parseColor("#ffa0a0a0"), Color.parseColor("#50a0a0a0"), Color.parseColor("#00a0a0a0")});
            shadowHeight = dp2px(8);
        }
    }

    /**
     * 销毁渐变阴影
     */
    private  void destroySectionShadow() {
        if (sectionViewShadow != null) {
            sectionViewShadow = null;
            shadowHeight = 0;
        }
    }

    private int dp2px(float dpvalue) {
        return (int) (dpvalue
                * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (pinnedHeaderListAdapter == null || sectionView == null) {
            return;
        }
        int pLeft = getListPaddingLeft();
        int pTop = getListPaddingTop();
        View view  = sectionView.view;
        canvas.save();
        // 为重画子view变换画布
        canvas.clipRect(pLeft, pTop, pLeft + view.getWidth(), pTop + view.getHeight() + shadowHeight);
        canvas.translate(getPaddingLeft(), distanceY+getPaddingTop());
        // 重画section view
        drawChild(canvas, view, getDrawingTime());
        // 画section阴影
        if (sectionViewShadow != null && distanceY >= 0) {
            sectionViewShadow.setBounds(view.getLeft(),
                                        view.getBottom(),
                                        view.getRight(),
                                        view.getBottom() + shadowHeight);
            sectionViewShadow.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if (adapter instanceof PinnedHeaderListAdapter) {
            pinnedHeaderListAdapter = (PinnedHeaderListAdapter) adapter;
        }
    }

    /**
     * 该方法实现外部onScroll回调
     * @param onScrollListener {@link OnScrollListener}
     */
    public void setOnPinnedListScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    /**
     * 浮动在顶部的section view管理类
     */
    private class SectionView {
        /**
         * 视图引用
         */
        public View view;
        /**
         * 该section view属于第几个section
         */
        public int section = -1;
    }

}
