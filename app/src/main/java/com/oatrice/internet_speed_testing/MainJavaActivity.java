package com.oatrice.internet_speed_testing;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.internet_speed_testing.InternetSpeedBuilder;
import com.example.internet_speed_testing.ProgressionModel;

import java.text.DecimalFormat;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import fr.bmartel.speedtest.utils.SpeedTestUtils;

public class MainJavaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnStart,btnStop;
    private Adapter adapter;
    InternetSpeedBuilder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        adapter = new Adapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);




        //checkSpeed();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder = new InternetSpeedBuilder(MainJavaActivity.this);
                builder.setOnEventInternetSpeedListener(new InternetSpeedBuilder.OnEventInternetSpeedListener() {
                    @Override
                    public void onDownloadProgress(int count, ProgressionModel progressModel) {

                    }

                    @Override
                    public void onUploadProgress(int count, ProgressionModel progressModel) {

                    }

                    @Override
                    public void onTotalProgress(int count, ProgressionModel progressModel) {
                        adapter.setDataList(count, progressModel);
                        if(progressModel.getProgressTotal()==50) {
                            Log.e("MainJavaActivity", "onDownloadTotalProgress " + convertBytesReadableMB(progressModel.getDownloadSpeed().longValue())  );
                        }else if(progressModel.getProgressTotal()==100) {
                            Log.e("MainJavaActivity", "onUploadTotalProgress " + convertBytesReadableMB(progressModel.getUploadSpeed().longValue()) );

                        }

                    }
                });
                builder.start("","", 1);//please use Skyline server urls
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.stop();
            }
        });
    }




    private void checkSpeed(){
        new SpeedTestTask().execute();
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String convertBytesReadableMB(long bytes){
        //long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return String.format("%.1f", bytes / 0x1p20);
    }

    public class SpeedTestTask extends AsyncTask<Void, Void, String> {
        int MB_1 = 1000000;
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        boolean isUploadStarted =false;
        @Override
        protected String doInBackground(Void... params) {



            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    // called when download/upload is finished
                    Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                    long speed = report.getTotalPacketSize();


                    if(!isUploadStarted) {
                        System.out.println("downloadspeed : " + convertBytesReadableMB(speed)  );
                        isUploadStarted=true;
                        speedTestSocket.startUpload("http://speedtest.tele2.net/upload.php", 1000000);

                    }else{
                        System.out.println("uploadspeed : " + convertBytesReadableMB(speed)  );
                    }
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download/upload error occur
                    Log.v("speedtest", "[PROGRESS] progress : " + errorMessage);
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    // called to notify download/upload progress
                    Log.v("speedtest", "[PROGRESS] progress : " + percent + "%");
                    Log.v("speedtest", "[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
                }
            });

           speedTestSocket.startDownload("https://www.skylinesynergy.net/AppTest/app-speed-test.zip");
            String fileName = SpeedTestUtils.generateFileName() + ".txt";
            Log.v("speedtest", "fileName : " + fileName);
            //


            return null;
        }
    }


}
