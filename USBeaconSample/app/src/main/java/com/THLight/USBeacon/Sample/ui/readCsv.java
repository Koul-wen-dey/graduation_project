package com.THLight.USBeacon.Sample.ui;
import android.content.Context;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


class readCsvThread extends Thread{
    Context context;
    ArrayList<String> table= new ArrayList<String>();
    public readCsvThread(Context context){
        this.context=context;
    }

    @Override
    public void run(){
        super.run();
        String filename="DB.csv";
        try{
            //FileReader in = new FileReader( context.getAssets().open(filename));
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename),"UTF-8"));
            String oneLine;
            String[] arrayOfLine;
            while((oneLine = reader.readLine()) != null){
                arrayOfLine = oneLine.split(",");
                for (String i : arrayOfLine) {
                    table.add(i);
                }
            }
            reader.close();
        }
        catch (Exception error){
            error.printStackTrace();
        }
    }


}
