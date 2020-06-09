package com.vsdrozd.blagodarie.ui.contacts;

import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class SwipeRefreshLayoutDataBinding {

    @BindingAdapter ("android:onRefresh")
    public static void setSwipeRefreshLayoutOnRefreshListener(SwipeRefreshLayout view, final ContactsViewModel viewModel) {
        view.setOnRefreshListener(viewModel::updateContactInfo);
    }
}
