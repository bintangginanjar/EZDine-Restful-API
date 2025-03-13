package restful.api.ezdine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import restful.api.ezdine.entity.OrderEntity;
import restful.api.ezdine.entity.OrderItemEntity;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Integer>{

    Optional<OrderItemEntity> findFirstByOrderEntityAndId(OrderEntity orderEntity, Integer id);

    List<OrderItemEntity> findAllByOrderEntity(OrderEntity orderEntity);

}
