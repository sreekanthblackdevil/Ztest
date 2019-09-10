package com.nightcoders.sreekanth.ztest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.internet_speed_testing.InternetSpeedBuilder;
import com.example.internet_speed_testing.ProgressionModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.nightcoders.sreekanth.ztest.Listeners.BackPressListener;
import com.nightcoders.sreekanth.ztest.Listeners.ChangeFragmentStateListener;
import com.nightcoders.sreekanth.ztest.Listeners.DataChangeListener;
import com.nightcoders.sreekanth.ztest.Listeners.LocationListener;
import com.nightcoders.sreekanth.ztest.Listeners.StartButtonClickListener;
import com.nightcoders.sreekanth.ztest.Supports.NetworkSupport;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.nightcoders.sreekanth.ztest.Literals.Core.MAIN_FRAGMENT_TAG;
import static com.nightcoders.sreekanth.ztest.Literals.Core.RESULT_FRAGMENT_TAG;
import static com.nightcoders.sreekanth.ztest.Literals.Core.START_FRAGMENT_TAG;

public class MainActivity extends FragmentActivity implements StartButtonClickListener,
        ChangeFragmentStateListener, BackPressListener {

    private DataChangeListener dataChangeListener;
    boolean start = false;
    boolean finished = false;
    private Thread thread;
    private MainFragment mainFragment;
    private ResultFragment resultFragment;
    private StartFragment startFragment;
    private InternetSpeedBuilder builder;
    private double upload = 0;
    private double download = 0;
    private double grandTotal = 0;
    private InterstitialAd mInterstitialAd;
    private CountDownTimer timer;
    private String currentCity;
    private LocationListener mlocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_id));

        myPhoneStateListener phoneStateListener = new myPhoneStateListener();
        TelephonyManager mTelManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        assert mTelManager != null;
        mTelManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        startFragment = new StartFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        assert fragmentManager != null;
        fragmentManager.beginTransaction()
                .add(R.id.frame, startFragment, START_FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        mlocationListener = startFragment;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            assert locationManager != null;
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                assert location != null;
                currentCity = getCityName(location.getLatitude(), location.getLongitude());
                if (!TextUtils.isEmpty(currentCity))
                    mlocationListener.OnLocationChanged(currentCity);
                else mlocationListener.OnLocationChanged("Not Found");
                Log.d("Latitude", String.valueOf(location.getLatitude()));
                Log.d("Longitude", String.valueOf(location.getLongitude()));
            } catch (Exception e) {
                e.printStackTrace();
                mlocationListener.OnLocationChanged("Not Found");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            assert locationManager != null;
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            assert location != null;
            currentCity = getCityName(location.getLatitude(), location.getLongitude());
            Log.d("Latitude", String.valueOf(location.getLatitude()));
            Log.d("Longitude", String.valueOf(location.getLongitude()));
            Toast.makeText(this, currentCity, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
        }
    }

    public void startTest() {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                builder = new InternetSpeedBuilder(MainActivity.this);
                builder.setOnEventInternetSpeedListener(new InternetSpeedBuilder.OnEventInternetSpeedListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDownloadProgress(int count, @NotNull final ProgressionModel progressModel) {
                        Log.d("SERVER", "" + progressModel.getDownloadSpeed());
                        BigDecimal bd = progressModel.getDownloadSpeed();
                        start = true;

                        final double d = bd.doubleValue();
                        download = d;
                        dataChangeListener.OnDownloadChanged(getFormat(d), convertMB(d),
                                getResources().getColor(R.color.violet), getFormatString(d));
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onUploadProgress(int count, @NotNull final ProgressionModel progressModel) {

                        start = true;
                        java.math.BigDecimal bigDecimal = new java.math.BigDecimal("" + progressModel.getUploadSpeed());
                        Log.d("NET_SPEED", "" + (float) (bigDecimal.longValue() / 1000000));
                        java.math.BigDecimal bd = progressModel.getUploadSpeed();
                        final double d = bd.doubleValue();
                        Log.d("SHOW_SPEED", "" + formatFileSize(d));
                        upload = d;

                        dataChangeListener.OnUploadChange(getFormat(d), convertMB(d),
                                getResources().getColor(R.color.sky_blue), getFormatString(d));
                        //start = false;
                    }

                    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
                    @Override
                    public void onTotalProgress(int count, @NotNull final ProgressionModel progressModel) {
                        java.math.BigDecimal downloadDecimal = progressModel.getDownloadSpeed();
                        final double downloadFinal = downloadDecimal.doubleValue();

                        java.math.BigDecimal uploadDecimal = progressModel.getUploadSpeed();
                        final double uploadFinal = uploadDecimal.doubleValue();
                        final double totalSpeedCount = (downloadFinal + uploadFinal) / 2;
                        grandTotal = totalSpeedCount;

                        float finalDownload = (downloadDecimal.longValue() / 1000000);
                        float finalUpload = (uploadDecimal.longValue() / 1000000);
                        final float totalassumtionSpeed = (finalDownload + finalUpload) / 2;

                        Log.d("COMPLETE", "complete");
                        dataChangeListener.OnTotalSpeedChanged(totalassumtionSpeed, getFormat(totalassumtionSpeed),
                                getResources().getColor(R.color.white), getFormatString(totalSpeedCount));

                    }

                });
                builder.start("http://2.testdebit.info/fichiers/1Mo.dat", 1);
            }
        });
        thread.start();
    }

    public String formatFileSize(double size) {

        String hrSize;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" ");
        } else if (g > 1) {
            hrSize = dec.format(g);
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" Mbps");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" Kbps");
        } else {
            hrSize = dec.format(size);
        }

        return hrSize;
    }

    public String getFormat(double size) {

        String hrSize;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" ");
        } else if (g > 1) {
            hrSize = dec.format(g);
        } else if (m > 1) {
            hrSize = dec.format(m);
        } else if (k > 1) {
            hrSize = dec.format(k);
        } else {
            hrSize = dec.format(size);
        }

        return hrSize;
    }

    float convertMB(double size) {
        return (float) ((size / 1024.0) / 1024.0);
    }

    public String getFormatString(double size) {

        String hrSize;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" ");
        } else if (g > 1) {
            hrSize = "Gbps";
        } else if (m > 1) {
            hrSize = "Mbps";
        } else if (k > 1) {
            hrSize = "Kbps";
        } else {
            hrSize = " ";
        }

        return hrSize;
    }

    @Override
    public void OnStartButtonClick() {
        MainFragment mFragment = (MainFragment) getFragment(MAIN_FRAGMENT_TAG);
        ResultFragment mFragment1 = (ResultFragment) getFragment(RESULT_FRAGMENT_TAG);
        if (NetworkSupport.isConnected(this)) {
            if (!(mFragment != null && mFragment.isVisible()) || (mFragment1 != null && mFragment1.isVisible())) {
                finished = false;
                upload = 0;
                download = 0;
                grandTotal = 0;
                if (thread != null && !thread.isInterrupted()) {
                    thread.interrupt();
                }
                openMainFragment();

                if (resultFragment != null) {
                    FragmentManager fr = getSupportFragmentManager();
                    fr.beginTransaction().detach(resultFragment).remove(resultFragment).commit();
                }
                startTest();
                dataChangeListener = mainFragment;
                new CountDownTimer(20000, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        if (!start && !thread.isAlive()) {
                            Toast.makeText(MainActivity.this, "Connection Timeout", Toast.LENGTH_SHORT).show();
                            closeMainFragment();
                            thread.interrupt();
                            if (timer != null)
                                timer.cancel();
                            //thread.interrupt();
                        } else {
                            checkThread();
                            timer = new CountDownTimer(25000, 1000) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    closeMainFragment();
                                    showDialog();
                                    thread.interrupt();
                                }
                            }.start();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (true) {
                                        if (!NetworkSupport.isConnected(MainActivity.this)) {
                                            closeMainFragment();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (timer != null)
                                                        timer.cancel();
                                                    Toast.makeText(MainActivity.this, "Connection Lost", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            thread.interrupt();
                                            break;
                                        }
                                        if (finished) {

                                            break;
                                        }
                                    }
                                }

                            }).start();
                        }
                    }
                }.start();
            }
        } else {
            showDialog();
        }
    }

    Fragment getFragment(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    @Override
    public void onBackPressed() {
        MainFragment mFragment = (MainFragment) getFragment(MAIN_FRAGMENT_TAG);
        ResultFragment mFragment1 = (ResultFragment) getFragment(RESULT_FRAGMENT_TAG);
        StartFragment mFragment2 = (StartFragment) getFragment(START_FRAGMENT_TAG);
        if (mFragment != null && mFragment.isVisible()) {
            if (!thread.isInterrupted()) {
                Toast.makeText(this, "Progress Running...", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onBackPressed();
        }
        if (mFragment1 != null && mFragment1.isVisible()) {
            super.onBackPressed();
        }
        if (mFragment2 != null && mFragment2.isVisible()) {
            finish();
        }
    }

    @Override
    public void OnDetachMainFragment() {
        if (thread.isAlive() || !thread.isInterrupted()) {
            builder = null;
            thread.interrupt();
        }
    }

    @Override
    public void OnAttachMainFragment() {

    }

    @Override
    public void OnResultFragmentAttach() {

    }

    @Override
    public void OnResultFragmentDetach() {

    }

    @Override
    public void OnBackPress() {

    }

    void checkThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Thread ", "Check Started..");
                double previous = upload;
                double prevDown = download;
                double prevTotal = grandTotal;
                finished = false;
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if ((upload != 0 && previous == upload)
                            && (download != 0 && prevDown == download)
                            && (grandTotal != 0 && prevTotal == grandTotal)) { // Task Finished
                        finished = true;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mInterstitialAd.loadAd(new AdRequest.Builder().build());

                                mInterstitialAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdLoaded() {
                                        super.onAdLoaded();
                                        if (mInterstitialAd.isLoaded())
                                            mInterstitialAd.show();
                                    }
                                });

                                //Toast.makeText(MainActivity.this, "Process Complete", Toast.LENGTH_SHORT).show();

                                String[] results = {getFormat(download), getFormat(upload),
                                        getFormat(grandTotal)};
                                String[] formats = {getFormatString(download), getFormatString(upload), getFormatString(grandTotal)};

                                resultFragment = new ResultFragment(results, formats);
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                assert fragmentManager != null;
                                fragmentManager.beginTransaction()
                                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                                        .addToBackStack("START_FRAGMENT")
                                        .replace(R.id.frame, resultFragment, "RESULT_FRAGMENT")
                                        .attach(resultFragment)
                                        .commit();
                                timer.cancel();
                                FragmentManager fr = getSupportFragmentManager();
                                fr.beginTransaction().detach(mainFragment).detach(startFragment).commit();
                                startFragment = new StartFragment();
                                mainFragment = new MainFragment();
                            }
                        });
                        break;
                    }
                    previous = upload;
                    prevDown = download;
                    prevTotal = grandTotal;
                }
                Log.d("Thread ", "Check Stopped");
            }
        }).start();
    }

    void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connection_lost);
        TextView ok = dialog.findViewById(R.id.ok_btn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private String getCityName(double lat, double lan) {
        String cityName = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lan, 10);
            if (addresses.size() > 0) {
                for (Address addr : addresses) {
                    if (addr.getLocality() != null && addr.getLocality().length() > 0) {
                        cityName = addr.getLocality();
                        Log.d("Loc", cityName);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            cityName = "Not Found !";
            Log.d("Location", " Not found!");
            e.printStackTrace();
        }
        Toast.makeText(this, cityName, Toast.LENGTH_SHORT).show();
        return cityName;
    }

    private void openMainFragment() {
        mainFragment = new MainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        assert fragmentManager != null;
        fragmentManager.beginTransaction()
                .replace(R.id.frame, mainFragment, MAIN_FRAGMENT_TAG)
                .setCustomAnimations(0, 0, R.anim.fade_in, R.anim.fade_out)
                .addToBackStack(START_FRAGMENT_TAG)
                .attach(mainFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        mlocationListener = mainFragment;
    }

    private void closeMainFragment() {
        FragmentManager fr = getSupportFragmentManager();
        fr.beginTransaction().detach(mainFragment).commit();
    }

    class myPhoneStateListener extends PhoneStateListener {
        int signalStrength = 0;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            this.signalStrength = signalStrength.getGsmSignalStrength();
            this.signalStrength = (2 * this.signalStrength) - 113;
            Log.d("Signal", String.valueOf(this.signalStrength));
        }
    }
}
