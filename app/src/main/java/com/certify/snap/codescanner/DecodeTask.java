package com.certify.snap.codescanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;

final class DecodeTask {

    private final byte[] mImage;
    private final com.certify.snap.codescanner.Point mImageSize;
    private final com.certify.snap.codescanner.Point mPreviewSize;
    private final com.certify.snap.codescanner.Point mViewSize;
    private final com.certify.snap.codescanner.Rect mViewFrameRect;
    private final int mOrientation;
    private final boolean mReverseHorizontal;

    public DecodeTask(@NonNull final byte[] image, @NonNull final com.certify.snap.codescanner.Point imageSize,
                      @NonNull final com.certify.snap.codescanner.Point previewSize, @NonNull final com.certify.snap.codescanner.Point viewSize,
                      @NonNull final com.certify.snap.codescanner.Rect viewFrameRect, final int orientation,
                      final boolean reverseHorizontal) {
        mImage = image;
        mImageSize = imageSize;
        mPreviewSize = previewSize;
        mViewSize = viewSize;
        mViewFrameRect = viewFrameRect;
        mOrientation = orientation;
        mReverseHorizontal = reverseHorizontal;
    }

    @Nullable
    @SuppressWarnings("SuspiciousNameCombination")
    public Result decode(@NonNull final MultiFormatReader reader) throws ReaderException {
        int imageWidth = mImageSize.getX();
        int imageHeight = mImageSize.getY();
        final int orientation = mOrientation;
        final byte[] image = com.certify.snap.codescanner.Utils.rotateYuv(mImage, imageWidth, imageHeight, orientation);
        if (orientation == 90 || orientation == 270) {
            final int width = imageWidth;
            imageWidth = imageHeight;
            imageHeight = width;
        }
        final com.certify.snap.codescanner.Rect frameRect =
                com.certify.snap.codescanner.Utils.getImageFrameRect(imageWidth, imageHeight, mViewFrameRect, mPreviewSize,
                        mViewSize);
        final int frameWidth = frameRect.getWidth();
        final int frameHeight = frameRect.getHeight();
        if (frameWidth < 1 || frameHeight < 1) {
            return null;
        }
        return com.certify.snap.codescanner.Utils.decodeLuminanceSource(reader,
                new PlanarYUVLuminanceSource(image, imageWidth, imageHeight, frameRect.getLeft(),
                        frameRect.getTop(), frameWidth, frameHeight, mReverseHorizontal));
    }
}
