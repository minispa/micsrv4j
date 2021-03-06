package com.github.minispa.micsrv.media.service;

import com.alibaba.fastjson.JSONArray;
import com.github.minispa.micsrv.media.model.Metadata;

import java.util.List;

public interface FFprobeProcService {

    JSONArray getStreams(String filePath);

    List<Metadata> getMetadata(String filePath);

}
