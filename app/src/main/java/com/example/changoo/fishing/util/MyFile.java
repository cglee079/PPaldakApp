package com.example.changoo.fishing.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

/**
 * Created by changoo on 2017-03-29.
 */

public class MyFile {

    private static File myFile = null;
    private static String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static void createFile(Context context) {
        String time= Time.getDateTime();
        String path = folderPath + "/" + time + ".txt";
        myFile=new File(path);
//        Toast.makeText(context,time+".txt 생성",Toast.LENGTH_SHORT).show();
    }

    public static File getMyFile() {
        return myFile;
    }
}
