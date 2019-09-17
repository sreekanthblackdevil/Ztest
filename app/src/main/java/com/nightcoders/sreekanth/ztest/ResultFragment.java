package com.nightcoders.sreekanth.ztest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nightcoders.sreekanth.ztest.Listeners.ChangeFragmentStateListener;
import com.nightcoders.sreekanth.ztest.Listeners.StartButtonClickListener;
import com.nightcoders.sreekanth.ztest.Supports.NetworkSupport;

import java.util.Objects;

import pl.pawelkleczkowski.customgauge.CustomGauge;


public class ResultFragment extends Fragment {

    private String total, downloadSpeed, uploadSpeed;
    private String downloaFormat, uploadFormat, totalFormatString;
    private ChangeFragmentStateListener changeFragmentStateListener;
    private CustomGauge gauge;
    private Button run;
    private StartButtonClickListener clickListener;

    ResultFragment(String[] result, String[] formats) {
        downloadSpeed = result[0];
        uploadSpeed = result[1];
        total = result[2];
        downloaFormat = formats[0];
        uploadFormat = formats[1];
        totalFormatString = formats[2];
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        TextView download = view.findViewById(R.id.download);
        TextView downloadFormatView = view.findViewById(R.id.download_format);
        TextView uploadFormatView = view.findViewById(R.id.upload_format);
        TextView upload = view.findViewById(R.id.uplaod);
        TextView totalSpeed = view.findViewById(R.id.total_speed);
        gauge = view.findViewById(R.id.gauge);
        TextView provider = view.findViewById(R.id.provider);
        TextView network = view.findViewById(R.id.network);
        run = view.findViewById(R.id.run);
        TextView totalFormat = view.findViewById(R.id.total_format);
        AdView adView = view.findViewById(R.id.adView);
        totalFormat.setText("TOTAL " + totalFormatString);
        totalSpeed.setText(total);
        download.setText(downloadSpeed);
        upload.setText(uploadSpeed);
        downloadFormatView.setText("DOWNLOAD " + downloaFormat);
        uploadFormatView.setText("UPLOAD " + uploadFormat);
        gauge.setVisibility(View.GONE);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animationRun();
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        String[] data = NetworkSupport.getNetworkProvider(Objects.requireNonNull(getActivity()).getApplicationContext());
        String prov = data[1];
        String net = data[0];
        provider.setText(prov);
        network.setText(net);
        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.btn_anim);
        shake.setRepeatCount(Animation.REVERSE);
        run.setAnimation(shake);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        changeFragmentStateListener = (ChangeFragmentStateListener) activity;
        changeFragmentStateListener.OnResultFragmentAttach();
        clickListener = (StartButtonClickListener) activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        changeFragmentStateListener.OnResultFragmentDetach();
    }

    private void animationRun() {
        gauge.setVisibility(View.VISIBLE);
        new CountDownTimer(2000, 1) {
            @Override
            public void onTick(long l) {
                gauge.setValue((int) l);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                run.setEnabled(true);
                gauge.setVisibility(View.GONE);
                run.setText("Run");
                try {
                    clickListener.OnStartButtonClick();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}