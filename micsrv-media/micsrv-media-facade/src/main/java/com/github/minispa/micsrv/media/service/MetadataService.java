package com.github.minispa.micsrv.media.service;

import com.github.minispa.micsrv.media.model.Metadata;

import java.util.List;

public interface MetadataService {

    List<Metadata> getMetadata(String filePath);

}
