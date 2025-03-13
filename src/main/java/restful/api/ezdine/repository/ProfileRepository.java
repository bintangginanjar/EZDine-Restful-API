package restful.api.ezdine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import restful.api.ezdine.entity.ProfileEntity;
import restful.api.ezdine.entity.UserEntity;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Integer>{

    Optional<ProfileEntity> findByFirstname(String firstname);

    Optional<ProfileEntity> findFirstByUserEntity(UserEntity userEntity);

}
