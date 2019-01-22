package com.pyp2p.jayjeet.pyp2pandroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int QR_CODE_SCANNER_REQUEST_CODE = 1;
    private static final int FILE_BROWSER_REQUEST_CODE = 2;

    String serverHost;
    Integer serverPort;
    String filePath = null;
    Button filePicker, scanQrCode;
    TextView fileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filePicker = findViewById(R.id.file_picker);
        scanQrCode = findViewById(R.id.scan_code);
        fileUrl = findViewById(R.id.file_url);

        filePicker.setOnClickListener(this);
        scanQrCode.setOnClickListener(this);
    }


    // Open the file browser intent
    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_BROWSER_REQUEST_CODE);
    }


    // This function is called after getting result from activity
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == FILE_BROWSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                filePath = uri.getPath().substring(10);
                fileUrl.setText(filePath);
            }
        } else if (requestCode == QR_CODE_SCANNER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                String[] addr = resultData.getData().toString().split(":");
                serverHost = addr[0];
                serverPort = Integer.parseInt(addr[1]);
                if (filePath != null) {
                    Thread cThread = new Thread(new ServerThread());
                    cThread.start();
                } else {
                    Toast.makeText(this, "No File Picked !", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // OnClick Listeners
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.file_picker) {
            performFileSearch();
        } else if (id == R.id.scan_code) {
            startActivityForResult(new Intent(this, BarcodeScanningActivity.class), QR_CODE_SCANNER_REQUEST_CODE);
        }
    }

    // File Sending Logic
    public class ServerThread implements Runnable {
        @Override
        public void run() {
            Socket socket = null;
            try {
                socket = new Socket(serverHost, serverPort);
            } catch (Exception e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Failed ! Try Again", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            File file = new File(filePath);


            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedInputStream bis = new BufferedInputStream(fis);
            try {
                bis.read(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            OutputStream os = null;
            try {
                os = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


