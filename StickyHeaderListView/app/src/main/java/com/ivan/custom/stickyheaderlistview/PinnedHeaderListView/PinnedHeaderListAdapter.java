package com.ivan.custom.stickyheaderlistview.PinnedHeaderListView;

import android.widget.BaseAdapter;

/**
 * PinnedHeaderListView的数据源适配器
 */
public abstract class PinnedHeaderListAdapter extends BaseAdapter {
    /**
     * 位于position的itme view是否section view
     * @param positon item的位置
     * @return 是否section view
     */
    public abstract boolean isSectionView(int positon);

    /**
     * 位于position的item view属于哪一个section
     * @param position item view的position
     * @return 所属的section view的position
     */
    public abstract int sectionOfItem(int position);
}
