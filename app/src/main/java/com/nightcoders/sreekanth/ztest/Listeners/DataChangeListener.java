package com.nightcoders.sreekanth.ztest.Listeners;

public interface DataChangeListener {
    void OnDownloadChanged(String speed, float total, String format);

    void OnUploadChange(String speed, float total, String formats);

//    void OnChangeProvider(String prov, String conn);

    void OnClearGuage();
}
