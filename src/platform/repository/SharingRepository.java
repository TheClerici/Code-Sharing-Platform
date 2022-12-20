package platform.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import platform.models.CodeModel;

import java.util.List;

@Repository
public interface SharingRepository extends CrudRepository<CodeModel, String> {
    List<CodeModel> findAllByRestrictedTimeAndRestrictedView(boolean restrictedTime, boolean restrictedView);
}
