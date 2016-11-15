//package zj.baselibrary.loadmore;
//
//import android.app.Activity;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//
//import org.byteam.superadapter.SuperAdapter;
//
///**
// * Created by ucmed on 2016/9/28.
// */
//
//public class FooterUtil {
//    public static int pageSize = 3;
//
//    public static LoadingFooter.State getFooterViewState(SuperAdapter adapter) {
//        if (adapter.getFooterView() != null) {
//            LoadingFooter footerView = (LoadingFooter) adapter.getFooterView();
//            return footerView.getState();
//        }
//        return LoadingFooter.State.Normal;
//    }
//
//    /**
//     * 简便流程 不包含LoadingFooter的初始化
//     * normal loading end
//     *
//     * @param adapter
//     * @param state
//     */
//    public static void setFooterViewState(SuperAdapter adapter, LoadingFooter.State state) {
//        if (adapter == null) return;
//        //第一次获得的数据量一页都没填满，就别加FooterView了
//        if (adapter.getItemCount() < pageSize) {
//            return;
//        }
//        LoadingFooter loadingFooter = (LoadingFooter) adapter.getFooterView();
//        if (loadingFooter == null) return;
//        loadingFooter.setState(state);
//    }
//
//    /**
//     * 简便流程 不包含LoadingFooter的初始化
//     * normal loading end
//     *
//     * @param adapter
//     * @param state
//     */
//    public static void setFooterViewState(SuperAdapter adapter, LoadingFooter.State state, View.OnClickListener errorListener) {
//        if (adapter == null) return;
//        //第一次获得的数据量一页都没填满，就别加FooterView了
//        if (adapter.getItemCount() < pageSize) {
//            return;
//        }
//        LoadingFooter loadingFooter = (LoadingFooter) adapter.getFooterView();
//        if (loadingFooter == null) return;
//        loadingFooter.setState(state);
//        if (state == LoadingFooter.State.NetWorkError) {
//            loadingFooter.setOnClickListener(errorListener);
//        }
//    }
//
//    /**
//     * 包含LoadingFooter的初始化 多一步recyclerView.scrollToPosition
//     * 支持error时的listener
//     *
//     * @param instance
//     * @param recyclerView
//     * @param adapter
//     * @param state
//     * @param errorListener 一般只在LoadingFooter.State为Error时需要 别的为null
//     */
//    public static void setFooterViewState(Activity instance, RecyclerView recyclerView, SuperAdapter adapter
//            , LoadingFooter.State state, View.OnClickListener errorListener) {
//
//        if (instance == null || instance.isFinishing()) {
//            return;
//        }
//
//        if (adapter == null) {
//            return;
//        }
//        //第一次获得的数据量一页都没填满，就别加FooterView了
//        if (adapter.getItemCount() < pageSize) {
//            return;
//        }
//
//        LoadingFooter footerView;
//
//        //已经有footerView了
//        if (adapter.getFooterView() != null) {
//            footerView = (LoadingFooter) adapter.getFooterView();
//            footerView.setState(state);
//        } else {
//            footerView = new LoadingFooter(instance);
//            footerView.setState(state);
//            adapter.addFooterView(footerView);
//        }
//
//        if (state == LoadingFooter.State.NetWorkError) {
//            footerView.setOnClickListener(errorListener);
//        }
//        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//    }
//}
