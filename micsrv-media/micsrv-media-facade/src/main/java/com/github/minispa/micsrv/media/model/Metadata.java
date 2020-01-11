package com.github.minispa.micsrv.media.model;


import java.io.Serializable;

public class Metadata implements Serializable {

    public int
            /** 1 */
            index,
    /**
     * 1920
     */
    width,
    /**
     * 1080
     */
    height,
    /**
     * 1920
     */
    coded_width,
    /**
     * 1080
     */
    coded_height,
    /**
     * 0
     */
    has_b_frames,
    /**
     * 40
     */
    level,
    /**
     * 1
     */
    refs,
    /**
     * 0
     */
    start_pts,
    /**
     * 873984
     */
    duration_ts;
    public String
            /** 1144:1143 */
            sample_aspect_ratio,
    /**
     * 143:254
     */
    display_aspect_ratio,
    /**
     * yuv420p
     */
    pix_fmt,
            chroma_location,
            is_avc,
            nal_length_size,
    /**
     * 30/1
     */
    r_frame_rate,
    /**
     * 30/1
     */
    avg_frame_rate,
    /**
     * 30/1
     */
    time_base,
    /**
     * 30/1
     */
    start_time,
    /**
     * 30/1
     */
    duration,
    /**
     * 30/1
     */
    bit_rate,
    /**
     * 30/1
     */
    bits_per_raw_sample,
    /**
     * 1707
     */
    nb_frames,
    /**
     * h264
     */
    codec_name,
    /**
     * H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10
     */
    codec_long_name,
    /**
     * Main
     */
    profile,
    /**
     * video
     */
    codec_type,
    /**
     * 1/60
     */
    codec_time_base,
    /**
     * avc1
     */
    codec_tag_string,
    /**
     * 0x31637661
     */
    codec_tag;

}
