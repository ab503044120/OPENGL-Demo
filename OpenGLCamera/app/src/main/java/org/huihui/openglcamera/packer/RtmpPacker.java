package org.huihui.openglcamera.packer;

import android.media.MediaCodec;

import org.huihui.openglcamera.CameraRender;
import org.huihui.openglcamera.sender.RtmpSender;

import java.nio.ByteBuffer;

import static org.huihui.openglcamera.packer.FlvPackerHelper.AUDIO_HEADER_SIZE;
import static org.huihui.openglcamera.packer.FlvPackerHelper.AUDIO_SPECIFIC_CONFIG_SIZE;
import static org.huihui.openglcamera.packer.FlvPackerHelper.VIDEO_HEADER_SIZE;
import static org.huihui.openglcamera.packer.FlvPackerHelper.VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE;

/**
 * Created by Administrator on 2017/5/27.
 */

public class RtmpPacker implements IPacker, AnnexbHelper.AnnexbNaluListener {
    public static final int FIRST_VIDEO = 1;
    public static final int FIRST_AUDIO = 2;
    public static final int AUDIO = 3;
    public static final int KEY_FRAME = 4;
    public static final int INTER_FRAME = 5;
    public static final int CONFIGRATION = 6;
    private static RtmpSender mRtmpSender;

    private OnPacketListener packetListener = new OnPacketListener() {
        @Override
        public void onPacket(byte[] data, int packetType) {

        }
    };
    private boolean isHeaderWrite;
    private boolean isKeyFrameWrite;

    private int mAudioSampleRate, mAudioSampleSize;
    private boolean mIsStereo;

    private AnnexbHelper mAnnexbHelper;

    public RtmpPacker() {
        mAnnexbHelper = new AnnexbHelper();
    }

    //    @Override
    public void setPacketListener(OnPacketListener listener) {
        packetListener = listener;
    }

    public static void startRtmp() {
        mRtmpSender = new RtmpSender();
        mRtmpSender.setAddress("rtmp://192.168.2.143/live/stream");
        mRtmpSender.setSenderListener(new RtmpSender.OnSenderListener() {
            @Override
            public void onConnecting() {

            }

            @Override
            public void onConnected() {
                CameraRender.recordingEnabled = true;
            }

            @Override
            public void onDisConnected() {
            }

            @Override
            public void onPublishFail() {

            }

            @Override
            public void onNetGood() {

            }

            @Override
            public void onNetBad() {

            }
        });
        mRtmpSender.connect();
    }

    @Override
    public void start() {
        mRtmpSender.start();
        mAnnexbHelper.setAnnexbNaluListener(this);
    }

    @Override
    public void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        mAnnexbHelper.analyseVideoData(bb, bi);
    }

    @Override
    public void onAudioData(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        if (packetListener == null || !isHeaderWrite || !isKeyFrameWrite) {
            return;
        }
        bb.position(bi.offset);
        bb.limit(bi.offset + bi.size);

        byte[] audio = new byte[bi.size];
        bb.get(audio);
        int size = AUDIO_HEADER_SIZE + audio.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeAudioTag(buffer, audio, false, mAudioSampleSize);
        mRtmpSender.onData(buffer.array(), AUDIO);
//        packetListener.onPacket(buffer.array(), AUDIO);
    }

    @Override
    public void stop() {
        isHeaderWrite = false;
        isKeyFrameWrite = false;
        mRtmpSender.stop();
        mAnnexbHelper.stop();
    }

    @Override
    public void onVideo(byte[] video, boolean isKeyFrame) {
        if (packetListener == null || !isHeaderWrite) {
            return;
        }
        int packetType = INTER_FRAME;
        if (isKeyFrame) {
            isKeyFrameWrite = true;
            packetType = KEY_FRAME;
        }
        //确保第一帧是关键帧，避免一开始出现灰色模糊界面
        if (!isKeyFrameWrite) {
            return;
        }
        int size = VIDEO_HEADER_SIZE + video.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeH264Packet(buffer, video, isKeyFrame);
//        packetListener.onPacket(buffer.array(), packetType);
        mRtmpSender.onData(buffer.array(), packetType);
    }

    @Override
    public void onSpsPps(byte[] sps, byte[] pps) {
        if (packetListener == null) {
            return;
        }
        //写入第一个视频信息
        writeFirstVideoTag(sps, pps);
        //写入第一个音频信息
        writeFirstAudioTag();
        isHeaderWrite = true;
    }

    private void writeFirstVideoTag(byte[] sps, byte[] pps) {
        int size = VIDEO_HEADER_SIZE + VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE + sps.length + pps.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeFirstVideoTag(buffer, sps, pps);
//        packetListener.onPacket(buffer.array(), FIRST_VIDEO);
        mRtmpSender.onData(buffer.array(), FIRST_VIDEO);
    }

    private void writeFirstAudioTag() {
        int size = AUDIO_SPECIFIC_CONFIG_SIZE + AUDIO_HEADER_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeFirstAudioTag(buffer, mAudioSampleRate, mIsStereo, mAudioSampleSize);
//        packetListener.onPacket(buffer.array(), FIRST_AUDIO);
        mRtmpSender.onData(buffer.array(), FIRST_AUDIO);
    }

    public void initAudioParams(int sampleRate, int sampleSize, boolean isStereo) {
        mAudioSampleRate = sampleRate;
        mAudioSampleSize = sampleSize;
        mIsStereo = isStereo;
    }
}
