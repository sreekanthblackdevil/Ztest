package com.nightcoders.sreekanth.ztest.Listeners;

public interface DataChangeListener {
    void OnDownloadChanged(String speed, float total, int color, String format);

    void OnUploadChange(String speed, float total, int color, String formats);

    void OnTotalSpeedChanged(float speed, String total, int color, String formats);

    void OnChangeProvider(String prov, String conn);
    void OnClearGuage(float f, String speed);
}
