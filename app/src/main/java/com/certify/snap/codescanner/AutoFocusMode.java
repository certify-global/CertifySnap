package com.certify.snap.codescanner;


/**
 * Code scanner auto focus mode
 *
 * @see com.certify.snap.codescanner.CodeScanner#setAutoFocusMode(AutoFocusMode)
 */
public enum AutoFocusMode {

    /**
     * Auto focus camera with the specified interval
     *
     * @see CodeScanner#setAutoFocusInterval(long)
     */
    SAFE,

    /**
     * Continuous auto focus, may not work on some devices
     */
    CONTINUOUS
}
