package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SimpleJdbcInsert
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {

//  private final JdbcTemplate template;
  private final NamedParameterJdbcTemplate template;
  private final SimpleJdbcInsert jdbcInsert;

  public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
    this.template = new NamedParameterJdbcTemplate(dataSource);
    this.jdbcInsert = new SimpleJdbcInsert(dataSource)
        .withTableName("Item")
        .usingGeneratedKeyColumns("id");
//        .usingColumns("item_name", "price", "quantity");
  }

  @Override
  public Item save(Item item) {
    SqlParameterSource param = new BeanPropertySqlParameterSource(item);
    // insert에서만 사용함.
    Number key = jdbcInsert.executeAndReturnKey(param);
    item.setId(key.longValue());
    return item;
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    String query = "update Item set item_name = :itemName," +
        " price = :price," +
        " quantity = :quantity" +
        " where id = :id";

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("itemName", updateParam.getItemName())
        .addValue("price", updateParam.getPrice())
        .addValue("quantity", updateParam.getQuantity())
        .addValue("id", itemId);

    template.update(query, param);
  }

  @Override
  public Optional<Item> findById(Long id) {
    String query = "select id, item_name, price, quantity from Item where id = ?";

    try {
      Map<String, Long> param = Map.of("id", id);

      Item item = template.queryForObject(query, param, itemRowMapper());
      return Optional.of(item);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    } catch (IncorrectResultSizeDataAccessException e) {
      throw new IllegalStateException("데이터가 2개 이상입니다.");
    }
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    String itemName = cond.getItemName();
    Integer maxPrice = cond.getMaxPrice();

    BeanPropertySqlParameterSource param = new BeanPropertySqlParameterSource(cond);

    String sql = "select id, item_name, price, quantity from Item";

    //동적 쿼리
    if (StringUtils.hasText(itemName) || maxPrice != null) {
      sql += " where (1 = 1) ";
    }
    boolean andFlag = false;
    if (StringUtils.hasText(itemName)) {
      sql += " and item_name like concat('%',:itemName,'%')";
    }
    if (maxPrice != null) {
      sql += " and price <= :maxPrice";
    }

    return template.query(sql, param, itemRowMapper());
  }

  // domain의 entity와 테이블의 필드가 자바빈 프로퍼티 규약을 지켜서 작성되어야 함.
  private RowMapper<Item> itemRowMapper() {
    return BeanPropertyRowMapper.newInstance(Item.class);
  }
}
