package com.pyp2p.jayjeet.pyp2pandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    Button connectButton;
    EditText host;
    EditText port;
    String serverIpAddress;
    Integer serverPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectButton = findViewById(R.id.connect_button);
        host = findViewById(R.id.host);
        port = findViewById(R.id.port);
        connectButton.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                Log.d("Permission","Granted");
            } else {
                requestPermission();
                Log.d("Permission","Asking permission");
            }
        }
        else
        {

        }

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }



    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.connect_button){
            serverIpAddress = host.getText().toString();
            serverPort = Integer.parseInt(port.getText().toString());
            Thread cThread = new Thread(new ServerThread());
            cThread.start();
        }
    }


    public class ServerThread implements Runnable{
        @Override
        public void run() {
            Socket socket = null;
            try {
                socket = new Socket(serverIpAddress, serverPort);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "img.png");

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


