package com.example.changoo.fishing.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.activity.LoginActivity;
import com.example.changoo.fishing.activity.MainActivity;
import com.example.changoo.fishing.activity.UserEditActivity;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.User;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import static android.content.Context.MODE_PRIVATE;

public class ForthFragment extends Fragment {
    public static final String TAG = "FORTH_FRAGMENT";

    ImageView mUserImgv;
    TextView mNameTv;
    TextView mPhoneNumberTv;
    TextView mIdTv;
    TextView mBirthTv;
    Button mLogoutBtn;
    Button mEditPrifileBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forth, container, false);
        mUserImgv = (ImageView) view.findViewById(R.id.imgv_user_image);

        mNameTv = (TextView) view.findViewById(R.id.tv_user_name);
        mPhoneNumberTv = (TextView) view.findViewById(R.id.tv_user_tel);
        mIdTv = (TextView) view.findViewById(R.id.tv_user_id);
        mBirthTv = (TextView) view.findViewById(R.id.tv_user_birth);
        mLogoutBtn = (Button) view.findViewById(R.id.btn_logout);
        mLogoutBtn.setOnClickListener(mButtonOnClickListener);
        mEditPrifileBtn = (Button) view.findViewById(R.id.btn_user_profile);
        mEditPrifileBtn.setOnClickListener(mButtonOnClickListener);

        User user = User.getInstance();

        if (user.getImageFile() == null)
            Picasso.with(this.getActivity()).load(R.drawable.image_default_user).transform(new CircleTransform()).into(mUserImgv);
        else
            Picasso.with(this.getActivity()).load(HttpManager.getUserImageURL() + user.getImageFile()).transform(new CircleTransform()).into(mUserImgv);


        mIdTv.setText(user.getId());

        String name = user.getName();
        if (name == null)
            name = "미입력";
        mNameTv.setText(name);

        String birth = user.getBirth();
        if (birth == null)
            birth = "미입력";
        mBirthTv.setText(birth);


        String phoneNumber = user.getPhoneNumber();
        if (phoneNumber == null)
            phoneNumber = "미입력";
        mPhoneNumberTv.setText(phoneNumber);

        return view;
    }

    Button.OnClickListener mButtonOnClickListener = new Button.OnClickListener() {

        Intent intent;
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_logout:
                    User.getInstance().clear();

                    // 값(Key Data) 삭제하기
                    SharedPreferences pref = getActivity().getSharedPreferences("mPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.clear();
                    editor.commit();

                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(getContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.btn_user_profile :
                    intent = new Intent(getActivity(), UserEditActivity.class);
                    getActivity().startActivityForResult(intent, MainActivity.USER_EDIT);
            }
        }
    };

    public void reload() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commitAllowingStateLoss();
    }

}
