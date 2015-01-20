package org.auscope.portal.server.security.oauth2;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<EavlUser, String>{

}
