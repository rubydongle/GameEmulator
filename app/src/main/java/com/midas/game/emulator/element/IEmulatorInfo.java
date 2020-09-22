package com.midas.game.emulator.element;

import android.util.SparseIntArray;

import com.midas.game.emulator.KeyboardProfile;
import com.midas.game.emulator.element.GfxProfile;
import com.midas.game.emulator.element.SfxProfile;

import java.util.List;

public interface IEmulatorInfo {

    String getName();

    boolean hasZapper();
    boolean supportsRawCheats();
    String getCheatInvalidCharsRegex();
    GfxProfile getDefaultGfxProfile();
    SfxProfile getDefaultSfxProfile();
    KeyboardProfile getDefaultKeyboardProfile();
    List<GfxProfile> getAvailableGfxProfiles();
    List<SfxProfile> getAvailableSfxProfiles();
    SparseIntArray getKeyMapping();
    int getNumQualityLevels();
    int[] getDeviceKeyboardCodes();
    String[] getDeviceKeyboardNames();
    String[] getDeviceKeyboardDescriptions();
    boolean isMultiPlayerSupported();

}
