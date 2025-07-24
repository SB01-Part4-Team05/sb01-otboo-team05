package com.part4.team05.sb01otbooteam05.domain.weather.batch.reader;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class OldWeatherItemReader extends JdbcPagingItemReader<UUID> {
  public OldWeatherItemReader(@Value("#{jobParameters['deleteTime']}") String deleteTimeStr,
      DataSource dataSource) {
    if (deleteTimeStr == null || deleteTimeStr.isBlank()) {
      throw new IllegalArgumentException("deleteTime 파라미터는 필수입니다.");
    }

    this.setDataSource(dataSource);
    this.setPageSize(1000);
    this.setRowMapper((rs, rowNum) -> UUID.fromString(rs.getString("id")));
    this.setQueryProvider(createQueryProvider());

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("deleteTime", LocalDateTime.parse(deleteTimeStr));
    this.setParameterValues(parameters);
  }

  private PagingQueryProvider createQueryProvider() {
    PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
    queryProvider.setSelectClause("id");
    queryProvider.setFromClause("weathers");
    queryProvider.setWhereClause("forecasted_at <= :deleteTime");
    queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));
    return queryProvider;
  }

}
