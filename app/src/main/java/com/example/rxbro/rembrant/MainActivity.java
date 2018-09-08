package com.example.rxbro.rembrant;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.rxbro.rembrant.view.RembrantView;

public class MainActivity extends AppCompatActivity {

    private RembrantView rembrantView;
    private AlertDialog.Builder currentAlertDialog;
    private ImageView widthImageView;
    private AlertDialog dialogLineWidth;
    private AlertDialog colorDialog;
    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar blueSeekBar;
    private SeekBar greenSeekBar;
    private View colorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rembrantView = findViewById(R.id.view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearId:
                rembrantView.clear();
                break;
            case R.id.saveId:
                rembrantView.saveToInternalStorage();
                break;
            case R.id.colorId:
                showColorDialog();
                break;
            case R.id.lineWidth:
                showLineWidthDialog();
                break;
            case R.id.eraseId:
                break;
        }
        if (item.getItemId() == R.id.clearId) {
            rembrantView.clear();
        }
        return super.onOptionsItemSelected(item);
    }
    void showColorDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.color_dialog, null);
        alphaSeekBar = view.findViewById(R.id.alphaSeekBar);
        redSeekBar = view.findViewById(R.id.redSeekBar);
        greenSeekBar = view.findViewById(R.id.greenSeekBar);
        blueSeekBar = view.findViewById(R.id.blueSeekBar);
        colorView = view.findViewById(R.id.colorView);
        alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        redSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        int color = rembrantView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        blueSeekBar.setProgress(Color.blue(color));
        greenSeekBar.setProgress(Color.green(color));
        Button setColorButton = view.findViewById(R.id.setColorButton);
        setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rembrantView.setDrawingColor(Color.argb(alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(), blueSeekBar.getProgress(), greenSeekBar.getProgress()));
                colorDialog.dismiss();
            }
        });
        currentAlertDialog.setView(view);
        currentAlertDialog.setTitle("Choose Color");
        colorDialog = currentAlertDialog.create();
        colorDialog.show();
    }
    void showLineWidthDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.width_dialog, null);
        final SeekBar widthSeekBar = view.findViewById(R.id.widthSeekBar);
        Button setLineWidthButton = view.findViewById(R.id.widthDialogButton);
        widthImageView = view.findViewById(R.id.imageViewId);
        setLineWidthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rembrantView.setLineWidth(widthSeekBar.getProgress());
                dialogLineWidth.dismiss();
                currentAlertDialog = null;
            }
        });
        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);
        widthSeekBar.setProgress(rembrantView.getLineWidth());
        currentAlertDialog.setView(view);
        dialogLineWidth = currentAlertDialog.create();
        dialogLineWidth.setTitle("Set Line Width");
        dialogLineWidth.show();
    }
    private SeekBar.OnSeekBarChangeListener colorSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            rembrantView.setBackgroundColor(Color.argb(alphaSeekBar.getProgress(), redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
            colorView.setBackgroundColor(Color.argb(alphaSeekBar.getProgress(), redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    private SeekBar.OnSeekBarChangeListener widthSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Paint p = new Paint();
            p.setColor(rembrantView.getDrawingColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);
            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370, 50, p);
            widthImageView.setImageBitmap(bitmap);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
}
