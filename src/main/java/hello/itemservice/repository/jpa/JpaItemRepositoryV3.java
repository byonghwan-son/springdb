package hello.itemservice.repository.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.item;

@Repository
@Transactional(readOnly = true)
public class JpaItemRepositoryV3 implements ItemRepository {

  private final EntityManager em;
  private final JPAQueryFactory queryFactory;

  public JpaItemRepositoryV3(EntityManager em) {
    this.em = em;
    this.queryFactory = new JPAQueryFactory(em);
  }

  @Override
  public Item save(Item item) {
    em.persist(item);
    return item;
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    findById(itemId).ifPresent((item) -> {
      item.setItemName(updateParam.getItemName());
      item.setPrice(updateParam.getPrice());
      item.setQuantity(updateParam.getQuantity());
    });
  }

  @Override
  public Optional<Item> findById(Long id) {
    Item item = em.find(Item.class, id);
    return Optional.ofNullable(item);
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    return queryFactory
        .select(item)
        .from(item)
        .where(likeItemName(cond.getItemName()), maxPrice(cond.getMaxPrice()))
        .fetch();
  }

  private BooleanExpression likeItemName(String itemName) {
    if (StringUtils.hasText(itemName)) {
      return item.itemName.like("%" + itemName + "%");
    }
    return null;
  }
  private BooleanExpression maxPrice(Integer maxPrice) {
    if (maxPrice != null) {
      return item.price.loe(maxPrice);
    }
    return null;
  }
}
