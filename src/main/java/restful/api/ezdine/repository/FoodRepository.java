package restful.api.ezdine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import restful.api.ezdine.entity.CategoryEntity;
import restful.api.ezdine.entity.FoodEntity;

public interface FoodRepository extends JpaRepository<FoodEntity, Integer>{

    Optional<FoodEntity> findFirstById(Integer id);

    Optional<FoodEntity> findFirstByCategoryEntityAndId(CategoryEntity category, Integer id);

    Optional<FoodEntity> findByName(String name);
}
