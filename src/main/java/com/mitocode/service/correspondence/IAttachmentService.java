package com.mitocode.service.correspondence;

import com.mitocode.model.correspondence.Correspondence;
import com.mitocode.model.correspondence.CorrespondenceAttachment;

import java.util.List;

public interface IAttachmentService {
    List<CorrespondenceAttachment> getAttachmentsByCorrespondence(Correspondence correspondence);

    CorrespondenceAttachment saveAttachment(CorrespondenceAttachment attachment);
}
