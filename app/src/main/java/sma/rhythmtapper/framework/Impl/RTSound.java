package sma.rhythmtapper.framework.Impl;

import android.media.SoundPool;

import sma.rhythmtapper.framework.Sound;

public class RTSound implements Sound {
    private int soundId;
    private SoundPool soundPool;

    public RTSound(SoundPool soundPool, int soundId) {
        this.soundId = soundId;
        this.soundPool = soundPool;
    }

    @Override
    public void play(float volume) {
        soundPool.play(soundId, volume, volume, 0, 0, 1);
    }

    @Override
    public void dispose() {
        soundPool.unload(soundId);
    }

}