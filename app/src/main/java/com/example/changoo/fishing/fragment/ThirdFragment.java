package com.example.changoo.fishing.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.changoo.fishing.activity.FishInfoActivity;
import com.example.changoo.fishing.adapter.RankAdapter;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.R;
import com.example.changoo.fishing.util.Sort;
import com.example.changoo.fishing.util.Time;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

public class ThirdFragment extends Fragment {
    private static final String TAG = "THIRD_FRAGMENT";
    private SwipeRefreshLayout mThisSwipeLayout = null;

    //가상의 물고기 데이터
    private ArrayList<Fish> mData = null;
    private ListView mFishListView;

    /************************
     * For Rank Filter
     ************************/
    private LinearLayout mFilterLayout = null;
    private TextView mFilterInfoTv = null;
    private ToggleButton mFilterTBtn = null;


    /************************
     * For Filter
     ******************************/
    private String mRankInfo;
    private String mPeriodInfo;
    private String mSpeciesInfo = "";
    private EditText mSpeciesEt = null;
    private Spinner mRankSpinner;
    private Spinner mRankPeriodSpinner;
    private ArrayAdapter<CharSequence> mCriteriaAdapter;
    private ArrayAdapter<CharSequence> mPeriodAdapter;

    String st_time = null;
    String end_time = null;

    /***************************************************************/

    /*************
     * for Top Fish
     ***************************/
    Fish mTopFish = null;
    private RankAdapter mRankAdapter;
    private LinearLayout mTopLayout = null;
    private ImageView mTopImgv = null;
    private TextView mTopUserIdTv = null;
    private TextView mTopNameTv = null;
    private TextView mTopSpciesTv = null;
    private TextView mTopDateTv = null;
    private TextView mTopMaxLabelTv = null;
    private TextView mTopMaxTv = null;
    private TextView mTopAvgLabelTv = null;
    private TextView mTopAvgTv = null;
    /***************************************************************/

    /**
     * Overide Method
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = new ArrayList<Fish>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        /**
         *Layout 새로고침 설정
         */

        mThisSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout_thirdFragment);
        mThisSwipeLayout.setEnabled(true);
        mThisSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromServer();
                mThisSwipeLayout.setRefreshing(false);
            }
        });

        /**********************************************************************************************************************/
        /*************************************Rank Filter 설정**************************************************************/


        /**************init***********/
        mRankInfo = "최대힘";
        mPeriodInfo = "모든 기간";
        st_time = "0/0/0";
        end_time = Time.getCurYear() + "/" + Time.getCurMonth() + "/" + Time.getCurDay();

        getDataFromServer();
        mRankAdapter = new RankAdapter(getActivity(), mData);

        mFilterLayout = (LinearLayout) view.findViewById(R.id.layout_filter);
        mFilterTBtn = (ToggleButton) view.findViewById(R.id.tbtn_rank_filter);
        mFilterTBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    mFilterLayout.setVisibility(View.VISIBLE);
                else
                    mFilterLayout.setVisibility(View.GONE);
            }
        });
        mFilterInfoTv = (TextView) view.findViewById(R.id.tv_filter_info);


        //List 어댑터 생성
        mCriteriaAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.criteria, R.layout.spinner_item);
        mCriteriaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mPeriodAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.period, R.layout.spinner_item);
        mPeriodAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        //FISH LIST 정렬
        mRankSpinner = (Spinner) view.findViewById(R.id.spinner_rank);
        mRankSpinner.setAdapter(mCriteriaAdapter);
        mRankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) { //정렬

                String[] items = getResources().getStringArray(R.array.criteria);
                mRankInfo = items[arg2];
                switch (arg2) {
                    case 0:
                        mRankAdapter.setCriteria(mRankAdapter.CRITERIA_MAXPOWER);
                        break;
                    case 1:
                        mRankAdapter.setCriteria(mRankAdapter.CRITERIA_AVGPOWER);
                        break;
                }
                notifyTopFishChanged();
                filterInfoTvChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Period LIST 정렬
        mRankPeriodSpinner = (Spinner) view.findViewById(R.id.spinner_rank_period);
        mRankPeriodSpinner.setAdapter(mPeriodAdapter);
        mRankPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) { //정렬
                String[] items = getResources().getStringArray(R.array.period);
                mPeriodInfo = items[arg2];
                switch (arg2) {
                    case 0: //전체
                        st_time = "0/0/0";
                        end_time = Time.getCurYear() + "/" + Time.getCurMonth() + "/" + Time.getCurDay();
                        break;
                    case 1: //일간
                        st_time = Time.getCurYear() + "/" + Time.getCurMonth() + "/" + Time.getCurDay();
                        end_time = Time.getCurYear() + "/" + Time.getCurMonth() + "/" + Time.getCurDay();
                        break;
                    case 2: //월간
                        st_time = Time.getCurYear() + "/" + Time.getCurMonth();
                        end_time = Time.getCurYear() + "/" + Time.getCurMonth() + "/" + Time.getCurDay();
                        break;
                }
                filterInfoTvChanged();
                getDataFromServer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpeciesEt = (EditText) view.findViewById(R.id.et_filter_species);
        mSpeciesEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSpeciesInfo = s.toString();
                filterInfoTvChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
                getDataFromServer();
            }
        });

        /**********************************************************************************************************************/


        /********************************************setTopView Layout***********************/
        mTopLayout = (LinearLayout) view.findViewById(R.id.layout_top_rank);
        mTopLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTopFish != null) {
                    Intent intent = new Intent(getActivity(), FishInfoActivity.class);
                    intent.putExtra("fish", mTopFish);
                    startActivity(intent);
                }
            }
        });
        mTopImgv = (ImageView) view.findViewById(R.id.imgv_rank_fishImg);
        mTopUserIdTv = (TextView) view.findViewById(R.id.tv_top_rank_user_id);
        mTopNameTv = (TextView) view.findViewById(R.id.tv_top_rank_name);
        mTopSpciesTv = (TextView) view.findViewById(R.id.tv_top_rank_species);
        mTopDateTv = (TextView) view.findViewById(R.id.tv_top_rank_date);
        mTopMaxLabelTv = (TextView) view.findViewById(R.id.tv_top_rank_max_label);
        mTopMaxTv = (TextView) view.findViewById(R.id.tv_top_rank_max);
        mTopAvgLabelTv = (TextView) view.findViewById(R.id.tv_top_rank_avg_label);
        mTopAvgTv = (TextView) view.findViewById(R.id.tv_top_rank_avg);
        /**************************************************************************************/

        /**
         *setFishListView
         */
        getDataFromServer();
        mRankAdapter = new RankAdapter(getActivity(), mData);

        mFishListView = (ListView) view.findViewById(R.id.rank_listview);
        mFishListView.setAdapter(mRankAdapter);
        mFishListView.setDivider(null);
        mFishListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    mThisSwipeLayout.setEnabled(true);
                } else
                    mThisSwipeLayout.setEnabled(false);
            }
        });
        mFishListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), FishInfoActivity.class);
                intent.putExtra("fish", mData.get(i));
                startActivity(intent);
            }
        });


        //서버로부터 데이터 수신
        getDataFromServer();
        mRankAdapter.notifyDataSetChanged();
        notifyTopFishChanged();
        filterInfoTvChanged();
        return view;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getDataFromServer();
            filterInfoTvChanged();
        }
    }

    /**
     * Private Method
     */

    //데이터 삽임
    private void getDataFromServer() {
        //데이터 수신
        new HttpFishsAsyncTask().execute();
    }

    private void notifyTopFishChanged() {
        if (mData.size() == 0) {
            mTopLayout.setVisibility(View.GONE);
        } else {
            mTopLayout.setVisibility(View.VISIBLE);
            mTopFish = mData.get(0);

            if (mTopFish.getImageFile().equals("null"))
                Picasso.with(getActivity()).load(R.drawable.image_default_fish).transform(new CircleTransform()).into(mTopImgv);
            else
                Picasso.with(getActivity()).load(HttpManager.getFishImageURL() + mTopFish.getImageFile()).transform(new CircleTransform()).into(mTopImgv);

            mTopMaxLabelTv.setTextColor(getResources().getColor(R.color.colorGray));
            mTopMaxTv.setTextColor(getResources().getColor(R.color.colorBase));
            mTopAvgLabelTv.setTextColor(getResources().getColor(R.color.colorGray));
            mTopAvgTv.setTextColor(getResources().getColor(R.color.colorBase));

            mTopMaxLabelTv.setTypeface(null, Typeface.NORMAL);
            mTopMaxTv.setTypeface(null, Typeface.NORMAL);
            mTopAvgLabelTv.setTypeface(null, Typeface.NORMAL);
            mTopAvgTv.setTypeface(null, Typeface.NORMAL);

            if (mRankSpinner.getSelectedItemPosition() == mRankAdapter.CRITERIA_MAXPOWER) {
                mTopMaxLabelTv.setTextColor(Color.BLACK);
                mTopMaxTv.setTextColor(getResources().getColor(R.color.colorBlue));
                mTopMaxLabelTv.setTypeface(null, Typeface.BOLD);
                mTopMaxTv.setTypeface(null, Typeface.BOLD);
            } else if (mRankSpinner.getSelectedItemPosition() == mRankAdapter.CRITERIA_AVGPOWER) {
                mTopAvgLabelTv.setTextColor(Color.BLACK);
                mTopAvgTv.setTextColor(getResources().getColor(R.color.colorBlue));
                mTopAvgLabelTv.setTypeface(null, Typeface.BOLD);
                mTopAvgTv.setTypeface(null, Typeface.BOLD);
            }

            mTopUserIdTv.setText(mTopFish.getUser_id());
            mTopNameTv.setText(mTopFish.getName());
            mTopSpciesTv.setText("(" + mTopFish.getSpecies() + ")");
            mTopDateTv.setText(mTopFish.getDate());
            mTopMaxTv.setText(mTopFish.getMaxFower() + " F");
            mTopAvgTv.setText(mTopFish.getAvgFower() + " F");
        }
    }

    private void filterInfoTvChanged() {
        String filterInfo = "";
        filterInfo += mRankInfo;
        filterInfo += "을 기준으로 ";
        filterInfo += mPeriodInfo;
        filterInfo += " 동안 ";
        if (mSpeciesInfo.length() == 0)
            filterInfo += "전체";
        else
            filterInfo += mSpeciesInfo;
        filterInfo += " 어종을";
        filterInfo += " 랭크";
        mFilterInfoTv.setText(filterInfo);
    }

    /**
     * Http Service
     * this class get Fish List using userID
     */

    class HttpFishsAsyncTask extends AsyncTask<Void, Void, Void> {
        private String line = null;
        private String page = null;
        private String reqeustURL = null;
        private String parameter = null;
        private ArrayList<Fish> mDataFromServer = null;

        /**
         * MainThread
         */
        @Override
        protected void onPreExecute() {
            mDataFromServer = new ArrayList<>();
        }

        /**
         * Sub Thread
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {

                //URL 가져옴
                reqeustURL = HttpManager.getShowFishRankURL();

                //Parameter
                if (mSpeciesInfo.length() != 0)
                    parameter = URLEncoder.encode(mSpeciesInfo, "UTF-8");

                reqeustURL = reqeustURL + "?species=" + parameter + "&st_time=" + st_time + "&end_time=" + end_time;


                //String->URL
                URL url = new URL(reqeustURL);

                //Http Connect
                HttpURLConnection connect = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성.

                Log.d(TAG, "Connect  " + connect);
                connect.setRequestMethod("POST"); // post방식 통신
                connect.setDoOutput(true); // 쓰기모드 지정
                connect.setDoInput(true); // 읽기모드 지정
                connect.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                connect.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

                InputStream is = connect.getInputStream(); //input스트림 개방

                StringBuilder mBuilder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader mReader = new BufferedReader(new InputStreamReader(is)); //문자열 셋 세팅

                //요청이 정상적으로 전달되엇으면 HTTP_OK(200) 리턴
                //URL이 발견되지 않으면 HTTP_NOT_FOUND(404) 리턴
                //인증에 실패하면 HTTP_UNAUTHORIZED(401) 리턴
                int responseCode = connect.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "HTTP_OK :: " + responseCode);

                    // 버퍼의 웹문서 소스를 줄 단위로 읽어 저장
                    while ((line = mReader.readLine()) != null) {
                        mBuilder.append(line + "\n");
                    }
                }
                mReader.close();
                page = mBuilder.toString();
                Log.d(TAG, "HTTP_OK page: " + page);

                JSONObject meessage = new JSONObject(page);

                JSONArray fishs = meessage.getJSONArray("fishs");

                for (int i = 0; i < fishs.length(); i++) {
                    Gson gson = new Gson();
                    Fish fish = gson.fromJson(fishs.getJSONObject(i).toString(), Fish.class);
                    mDataFromServer.add(fish);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR URL" + e.toString());
            } catch (NoRouteToHostException nre) {
                nre.printStackTrace();
                Log.e(TAG, "Error " + nre.toString());
            } catch (IOException io) {
                io.printStackTrace();
                Log.e(TAG, "Error I/O " + io.toString());
            } catch (JSONException je) {
                je.printStackTrace();
                Log.e(TAG, "Error parsing data " + je.toString());
            } finally {
                return null;
            }
        }

        /**
         * MainThread
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mRankAdapter.refreshData(mDataFromServer);
            mRankAdapter.setCriteria(mRankSpinner.getSelectedItemPosition());
            mRankSpinner.setSelection(mRankSpinner.getSelectedItemPosition());
            ThirdFragment.this.notifyTopFishChanged();
        }
    }
}





