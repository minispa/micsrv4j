package com.github.minispa.micsrv.media.service;

import com.github.minispa.micsrv.media.model.MediaOperate;

public interface MediaComplexMarkService {
    /**
     * 正片水印并合并片尾
     *
     * @param mediaOperate#args(originPath, qrCodeText)
     * @return
     */
    String complexMark(MediaOperate mediaOperate);

    /**
     * 两个视频左右合并
     *
     * @param mediaOperate#args(backgroundPath, foregroundPath)
     * @return
     */
    String complexLRConcat(MediaOperate mediaOperate);

    /**
     * 两个视频上下合并
     *
     * @param mediaOperate#args(upPath, downPath)
     * @return
     */
    String complexUDConcat(MediaOperate mediaOperate);
}
