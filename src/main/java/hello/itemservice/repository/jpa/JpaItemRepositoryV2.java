package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaItemRepositoryV2 implements ItemRepository {

  private final SpringDataJpaItemRepository repository;

  @Override
  @Transactional
  public Item save(Item item) {
    return repository.save(item);
  }

  @Override
  @Transactional
  public void update(Long itemId, ItemUpdateDto updateParam) {
    Optional<Item> item = findById(itemId);

    item.ifPresent(i -> {
      i.setItemName(updateParam.getItemName());
      i.setPrice(updateParam.getPrice());
      i.setQuantity(updateParam.getQuantity());
    });
  }

  @Override
  public Optional<Item> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    if(StringUtils.hasText(cond.getItemName()) && cond.getMaxPrice() != null) {
      return repository.findItems("%" + cond.getItemName() + "%", cond.getMaxPrice());
    } else if(StringUtils.hasText(cond.getItemName())) {
      return repository.findByItemNameLike("%" + cond.getItemName() + "%");
    } else if(cond.getMaxPrice() != null) {
      return repository.findByPriceLessThanEqual(cond.getMaxPrice());
    }
    return repository.findAll();
  }
}
