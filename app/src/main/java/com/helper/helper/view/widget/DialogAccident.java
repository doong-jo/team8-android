package com.helper.helper.view.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.bcgdv.asia.lib.ticktock.TickTockView;
import com.helper.helper.R;
import com.helper.helper.controller.AddressManager;
import com.helper.helper.controller.EmergencyManager;
import com.helper.helper.controller.GoogleMapManager;
import com.helper.helper.controller.SMSManager;
import com.helper.helper.controller.UserManager;

import org.json.JSONException;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class DialogAccident {
    private final int VIBRATE_TIME = 1000;
    private final int VIBRATE_TERM_TIME = 500;

    private SweetAlertDialog m_dialog;
    private Vibrator m_vibrate;
    private MediaPlayer m_player;
    private Activity m_activity;
    private boolean m_bIsBackground;


    public void showDialog() {
        m_dialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            m_vibrate.vibrate(VibrationEffect.createWaveform(new long[]{VIBRATE_TIME, VIBRATE_TERM_TIME}, 0));
        } else {
            m_vibrate.vibrate(new long[]{VIBRATE_TIME, VIBRATE_TERM_TIME}, 0);
        }
        m_player.start();
    }

    public boolean getShowing() {
        return m_dialog.isShowing();
    }

    public DialogAccident(Activity activity, boolean bIsBackground) {

        m_activity = activity;
        m_bIsBackground = bIsBackground;

        m_vibrate = (Vibrator) m_activity.getSystemService(Context.VIBRATOR_SERVICE);

        m_player = MediaPlayer.create(m_activity, R.raw.siren);
        m_player.setLooping(true);

        AudioManager mAudioManager = (AudioManager) m_activity.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 9, 0);


        m_dialog =new SweetAlertDialog(m_activity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(m_activity.getString(R.string.emergency_dialog_title))
                .setCancelText(m_activity.getString(R.string.emergency_dialog_cancel))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        EmergencyManager.setEmergencyAlertState(false);
                        sweetAlertDialog.dismissWithAnimation();
                        m_vibrate.cancel();
                        mediaStop();
                        if( m_bIsBackground ) {
                            m_activity.finish();
                        }
                    }
                })
                .setConfirmText(m_activity.getString(R.string.emergency_dialog_send))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {
                        EmergencyManager.setEmergencyAlertState(false);
                        startAlertEmergencyContacts();
                        m_vibrate.cancel();
                        mediaStop();
                        EmergencyManager.insertAccidentinServer(m_activity, UserManager.getUser(),
                                EmergencyManager.getAccidentRolllover(), EmergencyManager.getAccidentAccel(),
                                EmergencyManager.getAccLocation(),true);
                    }
                });

        final DialogTickTock tickTockDlg = new DialogTickTock(m_activity, EmergencyManager.EMERGENCY_WAITING_ALERT_SECONDS);
        tickTockDlg.setOnTickListener(new TickTockView.OnTickListener() {
            @Override
            public String getText(long timeRemainingInMillis) {
                int seconds = (int) (timeRemainingInMillis / 1000) % 60;
                if( seconds == 0 ) {
                    startAlertEmergencyContacts();
                    tickTockDlg.setOnTickListener(null);
                    EmergencyManager.insertAccidentinServer(m_activity, UserManager.getUser(),
                            EmergencyManager.getAccidentRolllover(), EmergencyManager.getAccidentAccel(),
                            EmergencyManager.getAccLocation(), true);
                }
                return String.valueOf(seconds).concat("s");
            }
        });

        m_dialog.setCustomView(tickTockDlg);

        m_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if( m_dialog.getAlerType() != SweetAlertDialog.SUCCESS_TYPE ) {
                    EmergencyManager.insertAccidentinServer(m_activity, UserManager.getUser(),
                            EmergencyManager.getAccidentRolllover(), EmergencyManager.getAccidentAccel(),
                            EmergencyManager.getAccLocation(), false);
                }
            }
        });
    }

    private void startAlertEmergencyContacts() {
        SMSManager.sendEmergencyMessages(
                m_activity,
                EmergencyManager.getEmergencyContacts(),
                EmergencyManager.getAccLocation(),
                AddressManager.getConvertLocationToAddress());


        m_dialog
                .setTitleText(m_activity.getString(R.string.emergency_dialog_send_completely))
                .setContentText(m_activity.getString(R.string.emergency_dialog_coming))
                .setConfirmText(m_activity.getString(R.string.dialog_ok))
                .showCancelButton(false)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        mediaStop();
                        if( m_bIsBackground ) {
                            m_activity.finish();
                        }
                    }
                })
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
    }

    private void mediaStop() {
        if( m_player != null ) {
            m_player.release();
            m_player = null;
        }
    }
}
