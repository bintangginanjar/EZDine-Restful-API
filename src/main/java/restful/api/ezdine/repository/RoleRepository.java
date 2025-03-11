package restful.api.ezdine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import restful.api.ezdine.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {

    Optional<RoleEntity> findByName(String name);
    
}
