package restful.api.ezdine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import restful.api.ezdine.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer>{

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findFirstByToken(String token);

}
