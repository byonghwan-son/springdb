package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaItemRepository implements ItemRepository {

  private final EntityManager em;

  @Override
  @Transactional
  public Item save(Item item) {
    em.persist(item);
    return item;
  }

  @Override
  @Transactional
  public void update(Long itemId, ItemUpdateDto updateParam) {
    Item item = em.find(Item.class, itemId);

    item.setItemName(updateParam.getItemName());
    item.setPrice(updateParam.getPrice());
    item.setQuantity(updateParam.getQuantity());
  }

  @Override
  public Optional<Item> findById(Long id) {
    Item item = em.find(Item.class, id);
    return Optional.ofNullable(item);
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    String query = "select i from Item i where 1 = 1 ";

    if(cond.getItemName() != null && !ObjectUtils.isEmpty(cond.getItemName())) {
      query += " and i.itemName like concat('%', :itemName, '%')";
    }

    if(cond.getMaxPrice() != null) {
      query += " and i.price <= :maxPrice";
    }

    TypedQuery<Item> setQuery = em.createQuery(query, Item.class);
    if(cond.getItemName() != null && !ObjectUtils.isEmpty(cond.getItemName())) {
      setQuery.setParameter("itemName", cond.getItemName());
    }

    if(cond.getMaxPrice() != null) {
      setQuery.setParameter("maxPrice", cond.getMaxPrice());
    }

    return setQuery
        .getResultList();
  }
}
