package com.nightcoders.sreekanth.ztest;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nightcoders.sreekanth.ztest.Listeners.ChangeFragmentStateListener;
import com.nightcoders.sreekanth.ztest.Listeners.DataChangeListener;
import com.nightcoders.sreekanth.ztest.Listeners.LocationListener;
import com.nightcoders.sreekanth.ztest.Supports.NetworkSupport;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class MainFragment extends Fragment implements DataChangeListener, LocationListener {

    private PointerSpeedometer gauge;
    private TextView downloadSpeed, uploadSpeed, totalSpeed;
    private TextView format;
    private TextView downloadFormat;
    private TextView uploadFormat;
    private ChangeFragmentStateListener changeFragmentStateListener;
    private ImageView icon;
    private ProgressBar progressBar;
    private TextView location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gauge = view.findViewById(R.id.gauge);
        downloadSpeed = view.findViewById(R.id.download);
        uploadSpeed = view.findViewById(R.id.uplaod);
        totalSpeed = view.findViewById(R.id.total_speed);
        gauge = view.findViewById(R.id.gauge);
        TextView provider = view.findViewById(R.id.provider);
        downloadFormat = view.findViewById(R.id.download_format);
        AdView adView = view.findViewById(R.id.adView);
        uploadFormat = view.findViewById(R.id.upload_format);
        format = view.findViewById(R.id.format);
        progressBar = view.findViewById(R.id.progress);
        location = view.findViewById(R.id.location);
        progressBar.setVisibility(View.VISIBLE);
        TextView network = view.findViewById(R.id.network);
        icon = view.findViewById(R.id.icon_format);
        String[] data = NetworkSupport.getNetworkProvider(Objects.requireNonNull(getActivity())
                .getApplicationContext());
        String prov = data[1];
        String net = data[0];
        provider.setText(prov);
        network.setText(net);

        gauge.speedTo(0);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        return view;
    }

    @Override
    public void onAttachFragment(@NotNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Activity activity = (Activity) Objects.requireNonNull(getActivity()).getApplicationContext();
        changeFragmentStateListener = (ChangeFragmentStateListener) activity;
        changeFragmentStateListener.OnAttachMainFragment();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDetach() {
        super.onDetach();
        if (isRemoving()) {
            changeFragmentStateListener.OnDetachMainFragment();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isAdded()) {
            changeFragmentStateListener.OnAttachMainFragment();
        }
    }


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        changeFragmentStateListener = (ChangeFragmentStateListener) activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void OnDownloadChanged(String speed, float total, String formats) {
        downloadSpeed.setText(speed);
        progressBar.setVisibility(View.GONE);
        totalSpeed.setText(speed);
        format.setText(formats);
        gauge.speedTo(total);
        changeGaugeAccent(getResources().getColor(R.color.violet));
        downloadFormat.setText("DOWNLOAD " + formats);

        //Animation
        Integer colorFrom = getResources().getColor(R.color.text_grey);
        Integer colorTo = getResources().getColor(R.color.white);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                downloadFormat.setTextColor((Integer) animator.getAnimatedValue());
                downloadSpeed.setTextColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_down));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void OnUploadChange(String speed, float total, String formats) {
        uploadSpeed.setText(speed);
        totalSpeed.setText(speed);
        format.setText(formats);
        changeGaugeAccent(getResources().getColor(R.color.sky_blue));
        progressBar.setVisibility(View.GONE);
        gauge.speedTo(total);
        uploadFormat.setText("UPLOAD " + formats);
        Integer colorFrom = getResources().getColor(R.color.text_grey);
        Integer colorTo = getResources().getColor(R.color.white);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                uploadFormat.setTextColor((Integer) animator.getAnimatedValue());
                uploadSpeed.setTextColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_up));
    }

    private void changeGaugeAccent(int color) {
        gauge.setSpeedometerColor(color);
        gauge.setIndicatorColor(color);
    }

    @Override
    public void OnLocationChanged(String cityName) {
        location.setText(cityName);
    }
}
