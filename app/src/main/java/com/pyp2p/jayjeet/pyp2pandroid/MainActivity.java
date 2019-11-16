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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int QR_CODE_SCANNER_REQUEST_CODE = 1;
    private static final int FILE_BROWSER_REQUEST_CODE = 2;
    private static String filePath = null;
    private static String fileName = null;
    private static String host = null;
    private static Integer port = null;
    private TextView fileUrlText, addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button filePickerButton = findViewById(R.id.file_picker_button);
        Button scanButton = findViewById(R.id.scan_button);
        Button sendButton = findViewById(R.id.send_button);
        fileUrlText = findViewById(R.id.file_url_text);
        addressText = findViewById(R.id.address_text);

        filePickerButton.setOnClickListener(this);
        scanButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_BROWSER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == FILE_BROWSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                FileUtils utils = new FileUtils(getApplicationContext(), uri);
                String selectedFilePath = utils.getPath();
                filePath = selectedFilePath;
                String[] uriParts = selectedFilePath.split("/");
                fileName = uriParts[uriParts.length - 1];
                fileUrlText.setText(selectedFilePath);
            }
        }
        else if (requestCode == QR_CODE_SCANNER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                try {
                    String[] addr = String.valueOf(resultData.getData()).split(":");
                    host = String.valueOf(addr[0]);
                    port = Integer.parseInt(addr[1]);
                    addressText.setText(host + ":" + port.toString());
                } catch (Exception e) {
                    Toast.makeText(
                            getApplicationContext(), "Wrong QR Code was Scanned !", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.file_picker_button) {
            performFileSearch();
        }
        else if (id == R.id.scan_button) {
            startActivityForResult(
                    new Intent(this, BarcodeScanningActivity.class), QR_CODE_SCANNER_REQUEST_CODE);
        }
        else if (id == R.id.send_button) {
            if (filePath != null && fileName != null) {
                if (host != null && port != null) {
                    Thread sendingThread = new Thread(new SendingRunnable(host, port));
                    sendingThread.start();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please scan the QR code", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "No File Picked", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class SendingRunnable implements Runnable {

        private String serverHost;
        private Integer serverPort;

        private SendingRunnable(String serverHost, Integer serverPort) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(this.serverHost, this.serverPort);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.println(fileName);
                writer.flush();
                writer.close();
                socket.close();
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
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
            OutputStream os;
            try {
                Socket socket = new Socket(this.serverHost, this.serverPort);
                bis.read(bytes, 0, bytes.length);
                os = socket.getOutputStream();
                os.write(bytes, 0, bytes.length);
                os.flush();
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                filePath = null;
                fileName = null;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileUrlText.setText(R.string.no_file_choosen);
                        Toast.makeText(getApplicationContext(), "File sent successfully :)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}

