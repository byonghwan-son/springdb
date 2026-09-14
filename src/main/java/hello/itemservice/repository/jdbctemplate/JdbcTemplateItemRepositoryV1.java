package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBCTemplate
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

  private final JdbcTemplate template;

  public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
    this.template = new JdbcTemplate(dataSource);
  }

  @Override
  public Item save(Item item) {
    String query = "insert into Item (item_name, price, quantity) values (?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();
    template.update(conn -> {
      // 자동 증가 키
      PreparedStatement ps = conn.prepareStatement(query, new String[]{"id"});
      ps.setString(1, item.getItemName());
      ps.setInt(2, item.getPrice());
      ps.setInt(3, item.getQuantity());
      return ps;
    }, keyHolder);

    long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
    item.setId(id);

    return item;
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    String query = "update Item set item_name = ?, price = ?, quantity = ? where id = ?";
    template.update(query,
        updateParam.getItemName(),
        updateParam.getPrice(),
        updateParam.getQuantity(),
        itemId);
  }

  @Override
  public Optional<Item> findById(Long id) {
    String query = "select id, item_name, price, quantity from Item where id = ?";
    try {
      Item item = template.queryForObject(query, itemRowMapper(), id);
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

    String sql = "select id, item_name, price, quantity from Item";

    //동적 쿼리
    if (StringUtils.hasText(itemName) || maxPrice != null) {
      sql += " where (1 = 1) ";
    }
    boolean andFlag = false;
    List<Object> param = new ArrayList<>();
    if (StringUtils.hasText(itemName)) {
      sql += " and item_name like concat('%',?,'%')";
      param.add(itemName);
    }
    if (maxPrice != null) {
      sql += " and price <= ?";
      param.add(maxPrice);
    }

    return template.query(sql, itemRowMapper(), param.toArray());
  }

  private RowMapper<Item> itemRowMapper() {
    return (rs, rowNum) -> {
      Item item = new Item();

      item.setId(rs.getLong("id"));
      item.setItemName(rs.getString("item_name"));
      item.setPrice(rs.getInt("price"));
      item.setQuantity(rs.getInt("quantity"));

      return item;
    };
  }
}
