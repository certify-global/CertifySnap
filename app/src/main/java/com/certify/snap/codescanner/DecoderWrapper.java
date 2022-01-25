package com.certify.snap.codescanner;

import android.hardware.Camera;

import androidx.annotation.NonNull;

final class DecoderWrapper {

    private final Camera mCamera;
    private final Camera.CameraInfo mCameraInfo;
    private final com.certify.snap.codescanner.Decoder mDecoder;
    private final com.certify.snap.codescanner.Point mImageSize;
    private final com.certify.snap.codescanner.Point mPreviewSize;
    private final com.certify.snap.codescanner.Point mViewSize;
    private final int mDisplayOrientation;
    private final boolean mReverseHorizontal;
    private final boolean mAutoFocusSupported;
    private final boolean mFlashSupported;

    public DecoderWrapper(@NonNull final Camera camera, @NonNull final Camera.CameraInfo cameraInfo,
                          @NonNull final com.certify.snap.codescanner.Decoder decoder, @NonNull final com.certify.snap.codescanner.Point imageSize,
                          @NonNull final com.certify.snap.codescanner.Point previewSize, @NonNull final com.certify.snap.codescanner.Point viewSize,
                          final int displayOrientation, final boolean autoFocusSupported,
                          final boolean flashSupported) {
        mCamera = camera;
        mCameraInfo = cameraInfo;
        mDecoder = decoder;
        mImageSize = imageSize;
        mPreviewSize = previewSize;
        mViewSize = viewSize;
        mDisplayOrientation = displayOrientation;
        mReverseHorizontal = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mAutoFocusSupported = autoFocusSupported;
        mFlashSupported = flashSupported;
    }

    @NonNull
    public Camera getCamera() {
        return mCamera;
    }

    @NonNull
    public Camera.CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    @NonNull
    public com.certify.snap.codescanner.Decoder getDecoder() {
        return mDecoder;
    }

    @NonNull
    public com.certify.snap.codescanner.Point getImageSize() {
        return mImageSize;
    }

    @NonNull
    public com.certify.snap.codescanner.Point getPreviewSize() {
        return mPreviewSize;
    }

    @NonNull
    public com.certify.snap.codescanner.Point getViewSize() {
        return mViewSize;
    }

    public int getDisplayOrientation() {
        return mDisplayOrientation;
    }

    public boolean shouldReverseHorizontal() {
        return mReverseHorizontal;
    }

    public boolean isAutoFocusSupported() {
        return mAutoFocusSupported;
    }

    public boolean isFlashSupported() {
        return mFlashSupported;
    }

    public void release() {
        mCamera.release();
        mDecoder.shutdown();
    }
}
