package com.certify.snap.bluetooth.bleCommunication;

import com.squareup.otto.Bus;

public final class BusProvider {
    private static final Bus BUS = new Bus();

    private BusProvider() {
    }

    public static Bus getInstance() {
        return BUS;
    }
}