package com.mitocode.service.correspondence;

import com.mitocode.dto.request.correspondence.ComplaintRequest;
import com.mitocode.model.Complaint;
import com.mitocode.service.ICRUD;

public interface IComplaintService extends ICRUD<Complaint, Long> {
    Complaint registerComplaint(ComplaintRequest dto);
}
