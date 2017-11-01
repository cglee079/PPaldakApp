package com.example.changoo.fishing.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.activity.DeviceListActivity;
import com.example.changoo.fishing.activity.MainActivity;
import com.example.changoo.fishing.adapter.NoticeAdapter;
import com.example.changoo.fishing.data.Data;
import com.example.changoo.fishing.data.DataManager;
import com.example.changoo.fishing.graphic.DataShower;
import com.example.changoo.fishing.graphic.LineGraph;
import com.example.changoo.fishing.model.Notice;
import com.example.changoo.fishing.util.Constants;
import com.example.changoo.fishing.util.Time;

import android.os.Handler;

import java.util.ArrayList;

public class FirstFragment extends Fragment {
    private boolean created = false;

    public static final String TAG = "FIRST_FRAGMENT";
    public static final String CONNECTED_DEVICE = "CONNECTED_DEVICE";

    public static final int DO_FIXED 	= 0;
    public static final int DO_NOFIXED 	= 1;
    public static final int DO_BITED 	= 2;
    public static final int DO_MISSING 	= 3;
    public static final int DO_CATHCED 	= 4;
    public static final int DO_FIGHTED 	= 5;


    private String mConnectedDevice = null;
    private boolean connected = false;

    private DataManager mDataManager = null;
    private Handler mDataHandler = null;


    /**
     * Unconected Layout
     */
    Button mDoConnectBtn = null;

    /**
     * Connected Layout
     */
    private MainActivity.DataLogicHandler mDataLogicHandler = null;

    private DataShower mDataShower;

    private TextView mMaxDataTv = null;
    private TextView mAvgDataTv = null;

    private TextView mNoticeMsgTv = null;
    private TextView mNoticeTimeTv = null;

    //notice 어댑터
    private NoticeAdapter mNoticeAdapter = null;
    private ListView mNoticeLv = null;
    private ArrayList<Notice> mNoticeData = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (created == false) {
            mDataHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switchDataMsg(msg);
                    super.handleMessage(msg);
                }
            };
            
            mDataManager = new DataManager(getActivity(), mDataHandler);

            if (getArguments() != null) {
                mConnectedDevice = getArguments().getString(FirstFragment.CONNECTED_DEVICE);
            }
        }
        created = true;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;

        //연결된 디바이스가 있는지 확인
        Log.i(TAG, "mConnectedDevice is " + mConnectedDevice);
        if (mConnectedDevice == null) {
            connected = false;
        } else {
            connected = true;
        }


        //연결된 디바이스가 없는 경우
        if (connected == false) {
            v = inflater.inflate(R.layout.fragment_first_unconnected, container, false);
            mDoConnectBtn = (Button) v.findViewById(R.id.btn_doConnect);
            mDoConnectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doScan(); //
                }
            });
        }

        //연결된 디바이스가 있는 경우
        else {
            v = inflater.inflate(R.layout.fragment_first_connected, container, false);

            mDataShower = (DataShower) v.findViewById(R.id.graph_data);
            mMaxDataTv = (TextView) v.findViewById(R.id.tv_maxData);
            mAvgDataTv = (TextView) v.findViewById(R.id.tv_avgData);

            mNoticeMsgTv = (TextView) v.findViewById(R.id.tv_notice_msg);
            mNoticeTimeTv = (TextView) v.findViewById(R.id.tv_notice_time);
            mNoticeTimeTv.setText(Time.getTime());

            mNoticeLv = (ListView) v.findViewById(R.id.lv_notice);
            mNoticeData = new ArrayList<>();
            mNoticeAdapter = new NoticeAdapter(getActivity(), mNoticeData);
            mNoticeLv.setAdapter(mNoticeAdapter);
            mNoticeLv.setDivider(null);

            Button btntest = (Button) v.findViewById(R.id.btn_test);
            btntest.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            double data1 = 10.0;
                            mDataManager.updateData(new Data(0.0, data1));

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            int time = 0;
                            while (time <= 1) {
                                double data = 0.0;
                                mDataManager.updateData(new Data(0.0, data));
                                time++;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }).start();
                }
            });

        }

        return v;

    }

    /********************
     * Getter abd Setter
     **************/

    public void setmDataLogicHandler(MainActivity.DataLogicHandler mDataLogicHandler) {
        this.mDataLogicHandler = mDataLogicHandler;
    }

    public void setmConnectedDevice(String mConnectedDevice) {
        this.mConnectedDevice = mConnectedDevice;
    }

    /**
     * Public Method
     */

    //프래그먼트 갱신
    public void reload() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commitAllowingStateLoss();
    }

    public void updateData(Data data) {

        //dataManager data갱신
        mDataManager.updateData(data);
        double maxPower = mDataManager.getMaxPower();
        double avgPower = mDataManager.getAvgPower();

        //그래픽 업데이트
        updateGraphic(data.getPower(), maxPower, avgPower);

    }

    /**
     * Private Method
     */

    //블루투스 선택 액티비티
    private void doScan() {
        Intent intent = new Intent(getActivity(), DeviceListActivity.class);
        getActivity().startActivityForResult(intent, Constants.REQUEST_CONNECT_DEVICE);
    }

    private void updateGraphic(Double power, Double maxPower, Double avgPower) {
        if (connected == true) {
            mMaxDataTv.setText(maxPower.toString() + "F");
            mAvgDataTv.setText(avgPower.toString() + "F");

            mDataShower.add(power);
            mDataShower.setMaxData(maxPower);
        }
    }


    private void switchDataMsg(Message msg) {
        Log.d(TAG, msg.what + "");
        //이전 공지를 ListView에 넣음
        mNoticeAdapter.add(new Notice(mNoticeTimeTv.getText().toString(), mNoticeMsgTv.getText().toString()));

        switch (msg.what) {
            case DataManager.DO_FIXED:
                mNoticeTimeTv.setText(Time.getTime());
                mNoticeMsgTv.setText(R.string.notice_fixed);
                break;

            case DataManager.DO_NOFIXED:
                mNoticeTimeTv.setText(Time.getTime());
                mNoticeMsgTv.setText(R.string.notice_nofixed);
                break;

            case DataManager.DO_BITED:
                mNoticeTimeTv.setText(Time.getTime());
                mNoticeMsgTv.setText(R.string.notice_bited);

                mDataLogicHandler.obtainMessage(this.DO_BITED,msg.obj).sendToTarget();
                break;

            case DataManager.DO_RUNAWAY:
                mNoticeTimeTv.setText(Time.getTime());
                mNoticeMsgTv.setText(R.string.notice_runaway);
                break;
            case DataManager.DO_MISSING:

                mNoticeTimeTv.setText(Time.getTime());
                mNoticeMsgTv.setText(R.string.notice_missing);

                mNoticeMsgTv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNoticeAdapter.add(new Notice(mNoticeTimeTv.getText().toString(), mNoticeMsgTv.getText().toString()));
                        mNoticeTimeTv.setText(Time.getTime());
                        mNoticeMsgTv.setText(R.string.notice_wait);
                    }
                }, 3000);
                mDataLogicHandler.obtainMessage(this.DO_MISSING).sendToTarget();
                break;

            case DataManager.DO_CATCHED:
                mNoticeTimeTv.setText(Time.getTime());
                mNoticeMsgTv.setText(R.string.notice_catched);
                mDataLogicHandler.obtainMessage(this.DO_CATHCED, msg.obj).sendToTarget();
                break;

            case DataManager.DO_FIGHTED:
                mNoticeTimeTv.setText(Time.getTime());
                mNoticeMsgTv.setText(R.string.notice_fighted);
                mDataLogicHandler.obtainMessage(this.DO_FIGHTED, msg.obj).sendToTarget();
                break;

        }
//

    }

}


