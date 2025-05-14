package com.mitocode.service.impl.file;

import com.mitocode.model.MediaFile;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.file.IMediaFileRepo;
import com.mitocode.service.file.IMediaFileService;
import com.mitocode.service.impl.CRUDImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl extends CRUDImpl<MediaFile, Integer> implements IMediaFileService {

    @Autowired
    private IMediaFileRepo repo;

    @Override
    protected IGenericRepo<MediaFile, Integer> getRepo() {
        return repo;
    }
}
