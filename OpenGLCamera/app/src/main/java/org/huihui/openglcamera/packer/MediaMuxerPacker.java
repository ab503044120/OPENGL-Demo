package org.huihui.openglcamera.packer;

import android.media.MediaCodec;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/5/27.
 */

public class MediaMuxerPacker implements IPacker {

    private final MediaCodec mMediaCodec;
    private MediaMuxer mMuxer;
    private int mTrackIndex;

    public MediaMuxerPacker(String outputFile, MediaCodec mediaCodec) {
        try {
            mMuxer = new MediaMuxer(outputFile.toString(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mMediaCodec = mediaCodec;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        bb.position(bi.offset);
        bb.limit(bi.offset + bi.size);
        mMuxer.writeSampleData(mTrackIndex, bb, bi);
    }

    @Override
    public void onAudioData(ByteBuffer bb, MediaCodec.BufferInfo bi) {

    }

    @Override
    public void start() {
        // now that we have the Magic Goodies, start the muxer
        mTrackIndex = mMuxer.addTrack(mMediaCodec.getOutputFormat());
        mMuxer.start();
    }

    @Override
    public void stop() {
        mMuxer.stop();
        mMuxer.release();
        mMuxer = null;
    }
}
