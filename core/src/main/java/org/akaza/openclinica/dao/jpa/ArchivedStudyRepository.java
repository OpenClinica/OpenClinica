package org.akaza.openclinica.dao.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by yogi on 4/13/17.
 */
@Repository
public interface ArchivedStudyRepository extends JpaRepository<ArchivedStudyEntity, String> {

}
