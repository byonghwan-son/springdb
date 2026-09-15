package hello.itemservice.repository.v2;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static hello.itemservice.domain.QItem.item;

@Repository
public class ItemQueryRepositoryV2 {
  private final JPAQueryFactory queryFactory;

  public ItemQueryRepositoryV2(EntityManager em) {
    this.queryFactory = new JPAQueryFactory(em);
  }

  public List<Item> findAll(ItemSearchCond searchCond) {

    return queryFactory.select(item)
        .from(item)
        .where(likeItemName(searchCond.getItemName()),
            maxPrice(searchCond.getMaxPrice()))
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
