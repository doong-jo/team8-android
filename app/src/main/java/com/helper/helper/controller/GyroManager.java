/*
 * Copyright (c) 10/15/18 1:51 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class GyroManager {
    private final static String TAG = GyroManager.class.getSimpleName() + "/DEV";

    public static SensorManager m_sensorManager = null;
    public static Sensor m_sensorAccel = null;
    public static Sensor m_sensorMag = null;

    private static long m_shockStateLastTime;

    public static float m_fPivotAzimuth = 0.0f;
    public static float m_fPivotPitch = 0.0f;
    public static float m_fPivotRoll = 0.0f;

    private static long m_fTimerStartTime = 0;

    private static final int AZIMUTH_PIVOT = 20;
    private static final int PITCH_PIVOT = 5;
    private static final int ROLL_PIVOT = 20;

    private static final int SHAKE_THRESHOLD = 30;
    private static final int ORIENTATION_LEFT = 944;
    private static final int ORIENTATION_RIGHT = 344;
    private static final int ORIENTATION_NONE = 892;
    private static final int EMERGENCY = 121;

    /**
     * Gyro
     **/
//    private SensorManager m_sensorManager;
//    private Sensor m_sensorAccel;
//    private Sensor m_sensorMag;
//    private float[] m_fMag = new float[3];
//    private float[] m_fAccel = new float[3];
//    private long m_shockStateLastTime;
//    private float m_beforeAccelX;
//    private float m_beforeAccelY;
//    private float m_beforeAccelZ;
    public static float getPivotRoll() {
        return m_fPivotRoll;
    }

    public static void setPivotRoll(float pivotRoll) {
        m_fPivotRoll = pivotRoll;
    }

    public static float getPivotAzimuth() {
        return m_fPivotAzimuth;
    }

    public static void setPivotAzimuth(float pivotAzimuth) {
        m_fPivotAzimuth = pivotAzimuth;
    }

    public static float getPivotPitch() {
        return m_fPivotPitch;
    }

    public static void setPivotPitch(float pivotPitch) {
        m_fPivotPitch = pivotPitch;
    }

    public static long getTimerStartTime() {
        return m_fTimerStartTime;
    }

    public static void setTimerStartTime(long timerStartTime) {
        m_fTimerStartTime = timerStartTime;
    }

    public static float[] getOrientation(float[] gravity, float[] geomagnetic) {
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
        result[0] = (float) Math.toDegrees(result[0]);

        // 0 이하의 값인 경우 360을 더한다.*
//        if(result[0] < 0) result[0] += 360;

        //방위(azimuth) 값
        result[0] = result[0];
        //경사도(pitch) 값
        result[1] = (float) Math.toDegrees(result[1]);
        //좌우회전(roll) 값
        result[2] = (float) Math.toDegrees(result[2]);

        return result;
    }

    public static void shockStateDetector(Activity activity, SensorEvent sensor) {
        long currentTime = System.currentTimeMillis();
        long gabOfTime = (currentTime - m_shockStateLastTime);
        float speed = 0;
        float accelX = sensor.values[0];
        float accelY = sensor.values[1];
        float accelZ = sensor.values[2];

        if (gabOfTime > 100) {
            m_shockStateLastTime = currentTime;

            double result = Math.sqrt( (accelX * accelX) + (accelY * accelY) + (accelZ * accelZ) );

            if( result > SHAKE_THRESHOLD ) {
                Log.d(TAG, "shockStateDetector: Shock!!!");
                Toast.makeText(activity, "Detect shock", Toast.LENGTH_SHORT).show();
            }

//            Log.d(TAG, "shockStateDetector: result : " + result);

//            speed = Math.abs(accelX + accelY + accelZ - m_beforeAccelX - m_beforeAccelY - m_beforeAccelZ) / gabOfTime * 10000;

//            if(count++ < 3 ) {
//                sum_speed += speed;
//                return;
//            } else {
//                count = 0;
//                speed = sum_speed/3;
//                sum_speed = 0;
//            }
//            if (speed > 700 && speed < SHAKE_THRESHOLD) {
//                Log.e("speed", "speed : " + speed);
//                if(bProcessing) {// 사용자 실수시 가볍게 흔들어서 취소
//                    hanSensor.removeMessages(1);
//                    hanSensor.sendEmptyMessageDelayed(2, 1000);
//                    showMsgl("취소 " + speed);
//                    return;
//                }
//            if (speed > SHAKE_THRESHOLD) {
//                Log.d(TAG, "shockStateDetector: ");
//                try {
//                    String strSMS1 = getString(R.string.sms_content) + "\n\n" + m_strAddressOutput;
//                    String strSMS2 = "https://google.com/maps?q=" + m_strLatitude + "," + m_strLogitude;
//
//                    List<ContactItem> contactItems;
//                    try {
//                        contactItems = FileManager.readXmlEmergencyContacts(this);
//
//                        for (ContactItem item :
//                                contactItems) {
//                            sendSMS(item.getPhoneNumber(), strSMS1);
//                            sendSMS(item.getPhoneNumber(), strSMS2);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

//                    sendSMS("+8201034823161", strSMS1);
//                    sendSMS("+8201034823161", strSMS2);
//                } catch (RuntimeException e) {
//                    e.printStackTrace();
//                }
//                if(!bProcessing) {
//                    bProcessing = true;
//                    hanSensor.sendEmptyMessage(0);
//                    showMsgl("충격 발생 " + speed);

//            }
//                else { // 2차 충격
//                    bProcessing = true;
//                    hanSensor.removeMessages(1); // 다이얼로그 연장
//                    hanSensor.sendEmptyMessageDelayed(1, send_time * 1000);
//                    showMsgl("2,3차 충격 " + speed);
//                }
//            }


//            m_beforeAccelX = accelX;
//            m_beforeAccelY = accelY;
//            m_beforeAccelZ = accelZ;

        }
    }
}

    /*
    public void changeLeftOrRightLEDOfRoll(float roll) {
        String writeStr = "";

        if (m_curInterrupt == EMERGENCY) {
            return;
        }

        if (roll >= ROLL_PIVOT) {
            Log.d(TAG, "onSensorChanged: right");
            writeStr = "0-07-1";
            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_RIGHT;
        } else if (roll <= -ROLL_PIVOT) {
            Log.d(TAG, "onSensorChanged: left");
            writeStr = "0-06-1";
            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_LEFT;
        }

        if (Math.abs(GyroManager.getPivotRoll()) >= 20 &&
                Math.abs(roll) < 20) {
            writeStr = m_curLED;

            if (writeStr == null) {
                return;
            }


            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_NONE;
        }
    }

}*/

