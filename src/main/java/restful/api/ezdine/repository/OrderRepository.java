package restful.api.ezdine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import restful.api.ezdine.entity.OrderEntity;
import restful.api.ezdine.entity.UserEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Integer>{

    Optional<OrderEntity> findFirstByOrderId(String orderId);

    Optional<OrderEntity> findFirstByUserEntityAndId(UserEntity user, Integer id);

    List<OrderEntity> findAllByUserEntity(UserEntity user);

}
