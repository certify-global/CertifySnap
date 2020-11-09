package com.certify.snap.view.voicebar;

/**
 * Bar Animation Listener
 *
 * @author Shailendra Gupta
 */

interface OnBarParamsAnimListener
{
    /**
     * Sends update to start animation
     */
    void start();

    /**
     * Sends update to stop animation
     */
    void stop();

    /**
     * Sends update to animate
     */
    void animate();
}