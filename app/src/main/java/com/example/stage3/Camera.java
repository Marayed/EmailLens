package com.example.stage3;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Camera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Button captureButton;
    private ImageView capturedImageView;
    private Mat mRgbaFrame;
    private TessBaseAPI tessBaseAPI;

    static {
        if (!OpenCVLoader.initDebug()) {
            // init opencv
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mOpenCvCameraView = findViewById(R.id.opencv_camera_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.setCvCameraViewListener(this);

        captureButton = findViewById(R.id.capture_button);
        capturedImageView = findViewById(R.id.captured_image_view);

        tessBaseAPI = new TessBaseAPI();

        copyTrainedDataToExternalStorage("eng.traineddata");

        tessBaseAPI.init(getExternalFilesDir(null).getAbsolutePath(), "eng");
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgbaFrame = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgbaFrame.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgbaFrame = inputFrame.rgba();
        return mRgbaFrame;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOpenCvCameraView.enableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }

        // Release resources
        if (tessBaseAPI != null) {
            tessBaseAPI.clear();
        }
    }

    public void captureImage(View view) {
        if (mRgbaFrame != null) {
            Bitmap bitmap = Bitmap.createBitmap(mRgbaFrame.cols(), mRgbaFrame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgbaFrame, bitmap);

            File file = new File(getExternalCacheDir(), "captured_image.png");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                String email = extractEmailFromImage(file.getAbsolutePath());

                if (email != null && !email.isEmpty()) {
                    //MongoDBHandler mongoDBHandler = new MongoDBHandler();
                    //mongoDBHandler.insertEmail(email);
                    //mongoDBHandler.close();

                    Toast.makeText(this, "Email detected and added to MongoDB: " + email, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No email address found.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String extractEmailFromImage(String imagePath) {
        tessBaseAPI.setImage(new File(imagePath));
        String recognizedText = tessBaseAPI.getUTF8Text();
        tessBaseAPI.clear();

        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}");
        Matcher matcher = pattern.matcher(recognizedText);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    private void copyTrainedDataToExternalStorage(String trainedDataName) {
        AssetManager assetManager = getAssets();
        String outputPath = new File(getExternalFilesDir(null), trainedDataName).getPath();

        try {
            InputStream inputStream = assetManager.open("tessdata/" + trainedDataName);
            OutputStream outputStream = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
