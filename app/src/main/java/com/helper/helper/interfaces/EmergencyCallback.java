
package com.helper.helper.interfaces;

public interface EmergencyCallback {
    void onResult(String result, double accel, double rollover);
    void onError(String result);
}
