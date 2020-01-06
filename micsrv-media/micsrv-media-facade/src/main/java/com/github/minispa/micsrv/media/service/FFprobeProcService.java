package com.github.minispa.micsrv.media.service;

import com.alibaba.fastjson.JSONArray;

public interface FFprobeProcService {

    JSONArray getStreams(String filePath);

}
