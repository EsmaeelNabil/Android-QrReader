package com.example.esmaeel.qrtest;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import java.util.List;

import static android.R.attr.button;

public class MainActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {
    private QRCodeReaderView qrCodeReaderView;
    private TextView resultTextView, copytv;
    boolean flash = false;
    String url;
    String lastScan = "ft";
    ConstraintLayout bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SetupView();
    }

    private void SetupView() {
        bg = (ConstraintLayout) findViewById(R.id.bg);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (hasFlash()) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TurnFlashOnOrOff(view);
            }
        });

        copytv = (TextView) findViewById(R.id.copytv);
        copytv.setVisibility(View.GONE);
        copytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!resultTextView.getText().equals("Scanning ...")) {
                    copyToClipboard(resultTextView.getText().toString().trim());
                } else {
                    Toast.makeText(MainActivity.this, "Scan some QR first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        resultTextView = (TextView) findViewById(R.id.rtv);
        resultTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PressLogic();
            }
        });

        qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);
        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(1000L);
        qrCodeReaderView.setBackCamera();
    }

    private void PressLogic() {
        url = resultTextView.getText().toString().trim();

        if (url.startsWith("www.") || url.startsWith("http://") || url.startsWith("https://")) {

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            MakeDialogForWebIntent("Are you sure, You want to open this URL ?");
        }

        if (!url.equals("Scanning ...")) {
            copyToClipboard(resultTextView.getText().toString().trim());
        }
    }

    public void MakeDialogForWebIntent(String Msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage(Msg);
        alertDialogBuilder.setPositiveButton("Open",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);

                    }
                });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void TurnFlashOnOrOff(View view) {
        if (flash) {
            flash = false;
            qrCodeReaderView.setTorchEnabled(false);
            Snackbar.make(view, "Flash is OFF", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            bg.setBackgroundColor(getResources().getColor(R.color.org));
        } else {
            flash = true;
            qrCodeReaderView.setTorchEnabled(true);
            Snackbar.make(view, "Flash is On", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            bg.setBackgroundColor(getResources().getColor(R.color.material_amber_500));
        }

    }

    public boolean hasFlash() {
        boolean s;
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            s = true;
        } else {
            s = false;
        }
        return s;
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        resultTextView.setText(text);
        copytv.setVisibility(View.VISIBLE);
        if (lastScan.equals("ft")) {
            MakeVibrate(200);
            lastScan = text;
        } else if (lastScan.equals(text)) {
            lastScan = text;
        } else {
            MakeVibrate(200);
            lastScan = text;
        }

    }

    private void MakeVibrate(int time) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }


    public void copyToClipboard(String copyText) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", copyText);
        clipboard.setPrimaryClip(clip);
        Toast toast = Toast.makeText(this, "Copied", Toast.LENGTH_SHORT);
        toast.show();
        MakeVibrate(100);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
