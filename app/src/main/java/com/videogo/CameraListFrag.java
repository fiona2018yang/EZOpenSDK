package com.videogo;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.devicemgt.EZDeviceSettingActivity;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.remoteplayback.list.PlayBackListActivity;
import com.videogo.remoteplayback.list.RemoteListContant;
import com.videogo.scan.main.CaptureActivity;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.ui.cameralist.EZCameraListAdapter;
import com.videogo.ui.cameralist.SelectCameraDialog;
import com.videogo.ui.message.EZMessageActivity2;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.ui.util.EZUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.pulltorefresh.IPullToRefresh;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshListView;

import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

import static com.videogo.EzvizApplication.getOpenSDK;


public class CameraListFrag extends Fragment implements View.OnClickListener, SelectCameraDialog.CameraItemClick {
    protected static final String TAG = "CameraListFrag";
    /**
     * 删除设备
     */
    private final static int SHOW_DIALOG_DEL_DEVICE = 1;
    private View view;
    private PullToRefreshListView mListView = null;
    private EZCameraListAdapter mAdapter = null;
    private LinearLayout mNoCameraTipLy = null;
    private LinearLayout mGetCameraFailTipLy = null;
    private TextView mCameraFailTipTv = null;
    private Button mAddBtn;
    private Button mUserBtn;
    private boolean bIsFromSetting = false;
    private int mClickType;
    private View mNoMoreView;
    public final static int TAG_CLICK_PLAY = 1;
    public final static int TAG_CLICK_REMOTE_PLAY_BACK = 2;
    public final static int TAG_CLICK_SET_DEVICE = 3;
    public final static int TAG_CLICK_ALARM_LIST = 4;
    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;
    private final static int LOAD_MY_DEVICE = 0;
    private final static int LOAD_SHARE_DEVICE = 1;
    private int mLoadType = LOAD_MY_DEVICE;
    private BroadcastReceiver mReceiver = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cameralist_fragment,container,false);
        initData();
        initView();
        return view;
    }

    private void initView() {
        mNoMoreView = getLayoutInflater().inflate(R.layout.no_device_more_footer, null);
        mAdapter = new EZCameraListAdapter(getActivity());
        mAdapter.setOnClickListener(new EZCameraListAdapter.OnClickListener() {
            @Override
            public void onPlayClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_PLAY;
                final EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                if (deviceInfo.getCameraNum() <= 0 || deviceInfo.getCameraInfoList() == null || deviceInfo.getCameraInfoList().size() <= 0) {
                    LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                    return;
                }
                if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                    LogUtil.d(TAG, "cameralist have one camera");
                    final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }

                    Intent intent = new Intent(getActivity(), EZRealPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                    startActivityForResult(intent, REQUEST_CODE);
                    return;
                }
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(CameraListFrag.this);
                selectCameraDialog.show(getActivity().getFragmentManager(), "onPlayClick");
            }

            @Override
            public void onDeleteClick(BaseAdapter adapter, View view, int position) {
                getActivity().showDialog(SHOW_DIALOG_DEL_DEVICE);
            }

            @Override
            public void onAlarmListClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_ALARM_LIST;
                final EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                Intent intent = new Intent(getActivity(), EZMessageActivity2.class);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceInfo.getDeviceSerial());
                startActivity(intent);
            }

            @Override
            public void onRemotePlayBackClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_REMOTE_PLAY_BACK;
                EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                if (deviceInfo.getCameraNum() <= 0 || deviceInfo.getCameraInfoList() == null || deviceInfo.getCameraInfoList().size() <= 0) {
                    LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                    return;
                }
                if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                    LogUtil.d(TAG, "cameralist have one camera");
                    EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }
                    Intent intent = new Intent(getActivity(), PlayBackListActivity.class);
                    intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    startActivity(intent);
                    return;
                }
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(CameraListFrag.this);
                selectCameraDialog.show(getActivity().getFragmentManager(), "RemotePlayBackClick");
            }

            @Override
            public void onSetDeviceClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_SET_DEVICE;
                EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), EZDeviceSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                intent.putExtra("Bundle", bundle);
                startActivity(intent);
                bIsFromSetting = true;
            }

            @Override
            public void onDevicePictureClick(BaseAdapter adapter, View view, int position) {

            }

            @Override
            public void onDeviceVideoClick(BaseAdapter adapter, View view, int position) {

            }

            @Override
            public void onDeviceDefenceClick(BaseAdapter adapter, View view, int position) {

            }
        });
        mListView = (PullToRefreshListView) view.findViewById(R.id.camera_listview);
        mNoCameraTipLy = (LinearLayout) view.findViewById(R.id.no_camera_tip_ly);
        mGetCameraFailTipLy = (LinearLayout) view.findViewById(R.id.get_camera_fail_tip_ly);
        mCameraFailTipTv = (TextView) view.findViewById(R.id.get_camera_list_fail_tv);
        mAddBtn = (Button) view.findViewById(R.id.btn_add);
        mUserBtn = (Button) view.findViewById(R.id.btn_user);
        mListView.setLoadingLayoutCreator(new PullToRefreshBase.LoadingLayoutCreator() {

            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter, PullToRefreshBase.Orientation orientation) {
                if (headerOrFooter)
                    return new PullToRefreshHeader(context);
                else
                    return new PullToRefreshFooter(context, PullToRefreshFooter.Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setOnRefreshListener(new IPullToRefresh.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView, boolean headerOrFooter) {
                getCameraInfoList(headerOrFooter);
            }
        });
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mListView.setAdapter(mAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);

        mUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popLogoutDialog();
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.debugLog(TAG, "onReceive:" + action);
                if (action.equals(Constant.ADD_DEVICE_SUCCESS_ACTION)) {
                    refreshButtonClicked();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ADD_DEVICE_SUCCESS_ACTION);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_list_refresh_btn:
            case R.id.no_camera_tip_ly:
                refreshButtonClicked();
                break;
            case R.id.camera_list_gc_ly:
//                Intent intent = new Intent(EZCameraListActivity.this, SquareColumnActivity.class);
//                startActivity(intent);
                break;
            default:
                break;
        }
    }
    /**
     * 刷新点击
     */
    private void refreshButtonClicked() {
        mListView.setVisibility(View.VISIBLE);
        mNoCameraTipLy.setVisibility(View.GONE);
        mGetCameraFailTipLy.setVisibility(View.GONE);
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setRefreshing();
    }
    @Override
    public void onCameraItemClick(EZDeviceInfo deviceInfo, int camera_index) {
        EZCameraInfo cameraInfo = null;
        Intent intent = null;
        switch (mClickType){
            case TAG_CLICK_PLAY:
                cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, camera_index);
                if (cameraInfo == null) {
                    return;
                }
                intent = new Intent(getActivity(),EZRealPlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case TAG_CLICK_REMOTE_PLAY_BACK:
                cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, camera_index);
                if (cameraInfo == null) {
                    return;
                }
                intent = new Intent(getActivity(), PlayBackListActivity.class);
                intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    /**
     * 从服务器获取最新事件消息
     */
    private void getCameraInfoList(boolean headerOrFooter) {
        if (getActivity().isFinishing()) {
            return;
        }
        new GetCamersInfoListTask(headerOrFooter).execute();
    }
    /**
     * 获取事件消息任务
     */
    private class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 0;
        public GetCamersInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mHeaderOrFooter) {
                mListView.setVisibility(View.VISIBLE);
                mNoCameraTipLy.setVisibility(View.GONE);
                mGetCameraFailTipLy.setVisibility(View.GONE);
            }
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... params) {
            if (getActivity().isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(getActivity())) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }
            try {
                List<EZDeviceInfo> result = null;
                if (mLoadType == LOAD_MY_DEVICE) {
                    if (mHeaderOrFooter) {
                        result = getOpenSDK().getDeviceList(0, 20);
                    } else {
                        result = getOpenSDK().getDeviceList((mAdapter.getCount() / 20) + (mAdapter.getCount() % 20 > 0 ? 1 : 0), 20);
                    }
                } else if (mLoadType == LOAD_SHARE_DEVICE) {
                    if (mHeaderOrFooter) {
                        result = getOpenSDK().getSharedDeviceList(0, 20);
                    } else {
                        result = getOpenSDK().getSharedDeviceList((mAdapter.getCount() / 20) + (mAdapter.getCount() % 20 > 0 ? 1 : 0), 20);
                    }
                }

                return result;

            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.debugLog(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            super.onPostExecute(result);
            mListView.onRefreshComplete();
            if (getActivity().isFinishing()) {
                return;
            }
            if (result != null) {
                if (mHeaderOrFooter) {
                    CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
                    for (LoadingLayout layout : mListView.getLoadingLayoutProxy(true, false).getLayouts()) {
                        ((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
                    }
                    mAdapter.clearItem();
                }
                if (mAdapter.getCount() == 0 && result.size() == 0) {
                    mListView.setVisibility(View.GONE);
                    mNoCameraTipLy.setVisibility(View.VISIBLE);
                    mGetCameraFailTipLy.setVisibility(View.GONE);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                } else if (result.size() < 10) {
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                } else if (mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                addCameraList(result);
                mAdapter.notifyDataSetChanged();
            }
            if (mErrorCode != 0) {
                onError(mErrorCode);
            }
        }
        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                    ActivityUtils.handleSessionException(getActivity());
                    break;
                default:
                    if (mAdapter.getCount() == 0) {
                        mListView.setVisibility(View.GONE);
                        mNoCameraTipLy.setVisibility(View.GONE);
                        mCameraFailTipTv.setText(Utils.getErrorTip(getActivity(), R.string.get_camera_list_fail, errorCode));
                        mGetCameraFailTipLy.setVisibility(View.VISIBLE);
                    } else {
                        Utils.showToast(getActivity(), R.string.get_camera_list_fail, errorCode);
                    }
                    break;
            }
        }
    }
    private void addCameraList(List<EZDeviceInfo> result) {
        int count = result.size();
        EZDeviceInfo item = null;
        for (int i = 0; i < count; i++) {
            item = result.get(i);
            mAdapter.addItem(item);
        }
    }
    /**
     * 弹出登出对话框
     *
     * @see
     * @since V1.0
     */
    private void popLogoutDialog() {
        AlertDialog.Builder exitDialog = new AlertDialog.Builder(getActivity());
        exitDialog.setTitle(R.string.exit);
        exitDialog.setMessage(R.string.exit_tip);
        exitDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LogoutTask().execute();
            }
        });
        exitDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        exitDialog.show();
    }
    private class LogoutTask extends AsyncTask<Void, Void, Void> {
        private Dialog mWaitDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getOpenSDK().logout();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();
            ActivityUtils.goToLoginAgain(getActivity());
            getActivity().finish();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (bIsFromSetting || (mAdapter != null && mAdapter.getCount() == 0)) {
            refreshButtonClicked();
            bIsFromSetting = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.shutDownExecutorService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CODE) {
            if (requestCode == REQUEST_CODE) {
                String deviceSerial = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
                int cameraNo = intent.getIntExtra(IntentConsts.EXTRA_CAMERA_NO, -1);
                int videoLevel = intent.getIntExtra("video_level", -1);
                if (TextUtils.isEmpty(deviceSerial)) {
                    return;
                }
                if (videoLevel == -1 || cameraNo == -1) {
                    return;
                }
                if (mAdapter.getDeviceInfoList() != null) {
                    for (EZDeviceInfo deviceInfo : mAdapter.getDeviceInfoList()) {
                        if (deviceInfo.getDeviceSerial().equals(deviceSerial)) {
                            if (deviceInfo.getCameraInfoList() != null) {
                                for (EZCameraInfo cameraInfo : deviceInfo.getCameraInfoList()) {
                                    if (cameraInfo.getCameraNo() == cameraNo) {
                                        cameraInfo.setVideoLevel(videoLevel);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
