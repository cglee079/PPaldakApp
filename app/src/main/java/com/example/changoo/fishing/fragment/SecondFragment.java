package com.example.changoo.fishing.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.activity.MainActivity;
import com.example.changoo.fishing.activity.MyFishInfoActivity;
import com.example.changoo.fishing.adapter.FishAdapter;
import com.example.changoo.fishing.graphic.CatchedFishFAB;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.model.User;
import com.example.changoo.fishing.util.Formatter;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SecondFragment extends Fragment {
    private static final String TAG = "SECOND_FRAGMENT";

    private SwipeRefreshLayout mThisSwipeLayout = null;

    //가상의 물고기 데이터
    private ArrayList<Fish> mData = null;

    private FishAdapter mFishAdapter;
    private ArrayAdapter<CharSequence> mSpinAdapter;
    private ListView mFishListView;

    private TextView mNumOfFishTv = null;

    String userID = null;

    /***************************
     * For Filter
     ********************************/
    private Spinner mSortSpinner;
    private EditText mNameSearchEt = null;
    private String mNameSearchInfo = "";

    /**
     * Overide Method
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = new ArrayList<Fish>();
        userID = User.getInstance().getId();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        /**
         *Layout 새로고침 설정
         */


        mThisSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout_secondFragment);
        mThisSwipeLayout.setEnabled(true);
        mThisSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromServer();
                mThisSwipeLayout.setRefreshing(false);
            }
        });

        mNumOfFishTv = (TextView) view.findViewById(R.id.tv_num_of_fish);

        /****************************For Filter********************************/
        //SortList 어댑터 생성
        mSpinAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.sort, R.layout.spinner_item);
        mSpinAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        //스피너 속성 FISH LIST 정렬
        mSortSpinner = (Spinner) view.findViewById(R.id.spinner_sort);
        mSortSpinner.setAdapter(mSpinAdapter);
        mSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) { //정렬
                switch (arg2) {
                    case 0:
                        mFishAdapter.setSorted(mFishAdapter.SORTED_NAME);
                        break;
                    case 1:
                        mFishAdapter.setSorted(mFishAdapter.SORTED_SPECIES);
                        break;
                    case 2:
                        mFishAdapter.setSorted(mFishAdapter.SORTED_MAXPOWER);
                        break;
                    case 3:
                        mFishAdapter.setSorted(mFishAdapter.SORTED_AVGPOWER);
                        break;
                    case 4:
                        mFishAdapter.setSorted(mFishAdapter.SORTED_DATETIME);
                        break;
                }

                mFishAdapter.notifyDataSetChanged();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mNameSearchEt = (EditText) view.findViewById(R.id.et_search_name);
        mNameSearchEt.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mNameSearchInfo = s.toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        getDataFromServer();
                    }
                }
        );
        /********************************************************************************************************/
        /********************************setFishListView*********************************/
        mFishAdapter = new FishAdapter(getActivity(), mData);

        mFishListView = (ListView) view.findViewById(R.id.listview);
        mFishListView.setDivider(null);
        mFishListView.setAdapter(mFishAdapter);
        mFishListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0 && view.getChildAt(0) != null && view.getChildAt(0).getTop() == 0) {
                    mThisSwipeLayout.setEnabled(true);
                } else
                    mThisSwipeLayout.setEnabled(false);
            }
        });

        mFishListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), MyFishInfoActivity.class);
                intent.putExtra("fish", mData.get(i));
                startActivity(intent);
            }
        });

        mFishListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            int index = -1;
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                index = i;
                    new AlertDialog.Builder(getContext())
                            .setItems(R.array.fab_long_click, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedIndex) {
                                    switch (selectedIndex) {
                                        case 0:
                                            deleteFish(index);
                                            break;
                                    }
                                }
                            }).show();

                    return true;
            }
        });

        /*************************************************************************/
        //서버로부터 데이터 수신
        getDataFromServer();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getDataFromServer();
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


    /**
     * Http Service
     * this class get Fish List using userID
     */

    class HttpFishsAsyncTask extends AsyncTask<Void, Void, Void> {
        private String line = null;
        private String page = null;
        private String reqeustURL = null;
        private String nameParameter = null;
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
                reqeustURL = HttpManager.getShowFishURL();

                //Parameter
                reqeustURL += "?id=" + userID;

                //Parameter
                if (mNameSearchInfo.length() != 0)
                    nameParameter = URLEncoder.encode(mNameSearchInfo, "UTF-8");

                reqeustURL = reqeustURL + "&fishname=" + nameParameter;

                //최종 접속 URL
                Log.d(TAG, "URL " + reqeustURL);

                //String->URL
                URL url = new URL(reqeustURL);

                //Http Connect
                HttpURLConnection connect = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성.

                Log.d(TAG, "Connect  " + connect);
                connect.setRequestMethod("GET"); // get방식 통신
                connect.setDoOutput(true); // 쓰기모드 지정
                connect.setDoInput(true); // 읽기모드 지정
                connect.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                connect.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

                InputStream is = connect.getInputStream(); //input스트림 개방

                StringBuilder mBuilder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader mReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅

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
                    Log.i(TAG, "fish From Sever : " + fish.toString());
                    mDataFromServer.add(fish);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR URL" + e.toString());
            } catch (ConnectException ce) {
                ce.printStackTrace();
                Log.e(TAG, "Error Connected " + ce.toString());
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
            mFishAdapter.refreshData(mDataFromServer);
            mNumOfFishTv.setText(mDataFromServer.size() + " 마리");
            mFishAdapter.setSorted(mSortSpinner.getSelectedItemPosition());
            mSortSpinner.setSelection(mSortSpinner.getSelectedItemPosition());

        }


    }

    public void deleteFish(int index){
        Fish mFish = mData.get(index);
        String mFishId = mFish.getId();
        String mFishImageFile = mFish.getImageFile();
        mData.remove(index);
        new HttpFishDeleteAsyncTask().execute(mFishId,mFishImageFile);
    }

    class HttpFishDeleteAsyncTask extends AsyncTask<String, Void, Void> {
        private String line = null;
        private String page = null;
        private String reqeustURL = null;

        /**
         * MainThread
         */
        @Override
        protected void onPreExecute() {
        }

        /**
         * Sub Thread
         */
        @Override
        protected Void doInBackground(String... params) {
            try {

                //URL 가져옴
                reqeustURL = HttpManager.getDeleteFishURL();

                //Parameter
                reqeustURL += Formatter.toFirstParameter("id",params[0]) + Formatter.toParameter("filename",params[1]);

                //최종 접속 URL
                Log.d(TAG, "URL " + reqeustURL);

                //String->URL
                URL url = new URL(reqeustURL);

                //Http Connect
                HttpURLConnection connect = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성.

                Log.d(TAG, "Connect  " + connect);
                connect.setRequestMethod("GET"); // get방식 통신
                connect.setDoOutput(true); // 쓰기모드 지정
                connect.setDoInput(true); // 읽기모드 지정
                connect.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                connect.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

                InputStream is = connect.getInputStream(); //input스트림 개방

                StringBuilder mBuilder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader mReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅

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

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR URL" + e.toString());
            } catch (ConnectException ce) {
                ce.printStackTrace();
                Log.e(TAG, "Error Connected " + ce.toString());
            } catch (IOException io) {
                io.printStackTrace();
                Log.e(TAG, "Error I/O " + io.toString());
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
            mFishAdapter.notifyDataSetChanged();
            mNumOfFishTv.setText(mData.size() + " 마리");
            mFishAdapter.setSorted(mSortSpinner.getSelectedItemPosition());
            mSortSpinner.setSelection(mSortSpinner.getSelectedItemPosition());
        }
    }
}


