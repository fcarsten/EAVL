package org.auscope.portal.server.web.service.jobtask;

import java.util.List;

import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<EAVLJob, Integer>{
    List<EAVLJob> findByUser(EavlUser user);

}
