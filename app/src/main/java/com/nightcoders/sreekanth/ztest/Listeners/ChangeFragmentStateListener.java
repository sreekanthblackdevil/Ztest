package com.nightcoders.sreekanth.ztest.Listeners;

public interface ChangeFragmentStateListener {
    void OnDetachMainFragment();

    void OnAttachMainFragment();

    void OnResultFragmentAttach();

    void OnResultFragmentDetach();
}
