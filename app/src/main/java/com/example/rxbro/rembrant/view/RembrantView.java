package com.example.rxbro.rembrant.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class RembrantView extends View {
    public static final float TOUCH_TOLERANCE = 10f;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathHashMap;
    private HashMap<Integer, Point> previousPointMap;
    private OutputStream outputStream;

    public RembrantView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        paintScreen = new Paint();
        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.GREEN);
        paintLine.setStrokeWidth(23);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        paintLine.setStyle(Paint.Style.STROKE);
        pathHashMap = new HashMap<>();
        previousPointMap = new HashMap<>();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);
        for (Integer key: pathHashMap.keySet()) {
            canvas.drawPath(pathHashMap.get(key), paintLine);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }
        invalidate(); // re-draw the screen, reset the color to white.
        return true;
    }

    public void clear() {
        pathHashMap.clear(); // remove all previous paths
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();

    }

    private void touchEnded(int pointerId) {
        Path path = pathHashMap.get(pointerId); // get corresponding Path
        bitmapCanvas.drawPath(path, paintLine); // draw to the bitmap
        path.reset();
    }

    private void touchStarted(float x, float y, int pointerId) {
        Path path; // store the path of the given touch
        Point point; // store the last point in the path
        if (pathHashMap.containsKey(pointerId)) {
            path = pathHashMap.get(pointerId);
            point = previousPointMap.get(pointerId);
        } else {
            path = new Path();
            pathHashMap.put(pointerId, path);
            point = new Point();
            previousPointMap.put(pointerId, point);
        }
        // move to the coordinates the user touched...
        path.moveTo(x, y);
        point.x = (int)x;
        point.y = (int)y;

    }
    private void touchMoved(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);
            if (pathHashMap.containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);
                Path path = pathHashMap.get(pointerId);
                Point point = previousPointMap.get(pointerId);
                // Calculate how far the user moved from the last update...
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    // if distance is significant enough to be considered a movement, then...
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);
                    // move the path to new location and store new coordinates.
                    point.x = (int)newX;
                    point.y = (int)newY;
                }
            }
        }
    }
    public int getDrawingColor() {
        return paintLine.getColor();
    }
    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }
    public int getLineWidth() {
        return (int)paintLine.getStrokeWidth();
    }
    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }
    public void saveImage() {
        String filename = "Rembrant" + System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        try {
            assert uri != null;
            outputStream = getContext().getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // this points to the image created
            try {
                outputStream.flush();
                outputStream.close();
                Toast message = Toast.makeText(getContext(), "Image Saved!", Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
                message.show();
            } catch (IOException e) {
                Toast message = Toast.makeText(getContext(), "Image Not Saved", Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
                message.show();
            }
        } catch (FileNotFoundException fne) {
            Toast message = Toast.makeText(getContext(), "Image Not Saved", Toast.LENGTH_LONG);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
            //fne.printStackTrace();
        }
    }
    public void saveImageToExternalStorage() {
        String filename = "Rembrant" + System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        try {
            assert uri != null;
            outputStream = getContext().getContentResolver().openOutputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
        try {
            outputStream.flush();
            outputStream.close();
            Toast message = Toast.makeText(getContext(), "Image Saved", Toast.LENGTH_LONG);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            MediaStore.Images.Media.insertImage(getContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public String getImagePath() {
        ContextWrapper cw = new ContextWrapper(getContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        return directory.getAbsolutePath();
    }
    public void saveToInternalStorage() {
        ContextWrapper cw = new ContextWrapper(getContext());
        String filename = "Rembrant" + System.currentTimeMillis();
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(directory, "profile.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
                Log.d("Image:", directory.getAbsolutePath());
                Toast message = Toast.makeText(getContext(), "Image saved + " + directory.getAbsolutePath(), Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
                message.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void loadImageFromStorage(String path) {
        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
