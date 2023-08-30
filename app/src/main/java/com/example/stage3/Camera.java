package com.example.stage3;

import android.app.Activity;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class Camera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
        private JavaCameraView cameraView;

        private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        cameraView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_camera);

            cameraView = findViewById(R.id.camera_view);
            cameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
            cameraView.setCvCameraViewListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            OpenCVLoader.initDebug();
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        @Override
        public void onPause() {
            super.onPause();
            if (cameraView != null)
                cameraView.disableView();
        }

        @Override
        public void onCameraViewStarted(int width, int height) {
            // Camera started, perform initialization if needed
        }

        @Override
        public void onCameraViewStopped() {
            // Camera stopped, release resources if needed
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            // Process each camera frame using OpenCV functions
            Mat frame = inputFrame.rgba();
            // Perform your OpenCV processing here
            return frame;
        }
}