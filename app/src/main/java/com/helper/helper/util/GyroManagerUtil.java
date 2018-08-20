package com.helper.helper.util;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public class GyroManagerUtil {
    public static SensorManager m_sensorManager = null;
    public static Sensor m_sensorAccel = null;
    public static Sensor m_sensorMag = null;

    public static float m_fPivotAzimuth = 0.0f;
    public static float m_fPivotPitch = 0.0f;
    private static long m_fTimerStartTime = 0;


    public static float getPivotAzimuth() {
        return m_fPivotAzimuth;
    }

    public static void setPivotAzimuth(float pivotazimuth) {
        GyroManagerUtil.m_fPivotAzimuth = pivotazimuth;
    }

    public static float getPivotPitch() {
        return m_fPivotPitch;
    }

    public static void setPivotPitch(float pivotPitch) {
        GyroManagerUtil.m_fPivotPitch = pivotPitch;
    }

    public static long getTimerStartTime() {
        return m_fTimerStartTime;
    }

    public static void setTimerStartTime(long timerStartTime) {
        GyroManagerUtil.m_fTimerStartTime = timerStartTime;
    }

    public static float[] getOrientation(float[] gravity, float[] geomagnetic)
    {
//        float[] R = new float[9];
//        float[] values = new float[3];
//
//        SensorManager.getRotationMatrix(R, null, gravity, geomagnetic);
//        SensorManager.getOrientation(R, values); // 함수를 호출하고 나면 values 에 값이 포함되어 반환된다.
//
//        return values;

        float[] result = new float[3];
        float[] rotation = new float[9];

        SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
        // 회전 매트릭스로 방향 데이터를 얻는다.
        SensorManager.getOrientation(rotation, result);

        // Radian 값을 Degree 값으로 변환한다.
        result[0] = (float)Math.toDegrees(result[0]);

        // 0 이하의 값인 경우 360을 더한다.*
//        if(result[0] < 0) result[0] += 360;

        //방위(azimuth) 값
        result[0] = result[0];
        //경사도(pitch) 값
        result[1] = (float)Math.toDegrees(result[1]);
        //좌우회전(roll) 값
        result[2] = (float)Math.toDegrees(result[2]);

        return result;
    }
}
