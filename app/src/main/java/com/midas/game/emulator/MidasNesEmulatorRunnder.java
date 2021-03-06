package com.midas.game.emulator;

import android.content.Context;

import com.midas.game.R;
import com.midas.game.cheats.Cheat;
import com.midas.game.utils.EmulatorUtils;
import com.midas.game.core.GameDescription;
import com.midas.game.utils.FileUtils;
import com.midas.game.utils.LogUtils;

import java.io.File;

public class MidasNesEmulatorRunnder extends EmulatorRunner {

    public MidasNesEmulatorRunnder(IEmulator emulator, Context context) {
        super(emulator, context);
    }

    public void setFastForwardEnabled(boolean enabled) {
        emulator.setFastForwardEnabled(enabled);
    }

    public void setFastForwardFrameCount(int frames) {
        emulator.setFastForwardFrameCount(frames);
    }

    public void copyAutoSave(int slot) {
        if (!emulator.isGameLoaded()) {
            throw new EmulatorException("game not loaded");
        }

        String md5 = emulator.getLoadedGame().md5;
        String base = EmulatorUtils.getBaseDir(context);
        String source = SlotUtils.getSlotPath(base, md5, 0);
        String target = SlotUtils.getSlotPath(base, md5, slot);
        String sourcePng = SlotUtils.getScreenshotPath(base, md5, 0);
        String targetPng = SlotUtils.getScreenshotPath(base, md5, slot);

        try {
            FileUtils.copyFile(new File(source), new File(target));
            FileUtils.copyFile(new File(sourcePng), new File(targetPng));

        } catch (Exception e) {
            throw new EmulatorException(R.string.act_emulator_save_state_failed);
        }
    }

    public int enableCheats(Context ctx, GameDescription game) {
        int numCheats = 0;

        for (String cheatChars : Cheat.getAllEnableCheats(ctx, game.checksum)) {
            if (cheatChars.contains(":")) {
                if (EmulatorHolder.getmEmulatorInfo().supportsRawCheats()) {
                    int[] rawValues = null;

                    try {
                        rawValues = Cheat.rawToValues(cheatChars);

                    } catch (Exception e) {
                        throw new EmulatorException(
                                R.string.act_emulator_invalid_cheat, cheatChars);
                    }
                    enableRawCheat(rawValues[0], rawValues[1], rawValues[2]);
                } else {
                    throw new EmulatorException(R.string.act_emulator_invalid_cheat, cheatChars);
                }

            } else {
                enableCheat(cheatChars.toUpperCase());
            }

            numCheats++;
        }

        return numCheats;
    }

    public void benchMark() {
        emulator.reset();
        long t1 = System.currentTimeMillis();

        for (int i = 0; i < 3000; i++) {
            emulator.emulateFrame(0);

            try {
                Thread.sleep(2);

            } catch (Exception ignored) {
            }
        }

        long t2 = System.currentTimeMillis();
        LogUtils.e("benchmark", "bechmark: " + (t2 - t1) / 1000f);
    }
}
