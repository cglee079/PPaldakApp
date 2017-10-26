package com.example.changoo.fishing.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.util.MyFile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ReadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        TextView mReadTv=(TextView)this.findViewById(R.id.tv_read);
        String str, str1="";
        try {
            FileInputStream fis= new FileInputStream(MyFile.getMyFile());
            BufferedReader bufferReader=new BufferedReader(new InputStreamReader(fis));

            while( (str = bufferReader.readLine()) != null ) {    // str에 txt파일의 한 라인을 읽어온다
                str1 += str;
                str1 += "\n";
            }// 읽어온 라인을 str1에 추가한다

            Log.d("READ",str1);
        }catch (Exception e){
            e.printStackTrace();
        }

        mReadTv.setText(str1);
    }
}
