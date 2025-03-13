package restful.api.ezdine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import restful.api.ezdine.entity.CategoryEntity;
import restful.api.ezdine.entity.UserEntity;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer>{

    Optional<CategoryEntity> findByName(String name);

    Optional<CategoryEntity> findFirstByUserEntityAndId(UserEntity user, Integer id);
}
