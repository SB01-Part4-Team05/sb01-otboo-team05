package com.part4.team05.sb01otbooteam05.domain.attribute.repository;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {

  Optional<AttributeDefinition> findByName(String name);

  @Query("""
    SELECT a FROM AttributeDefinition a
    WHERE (:cursor IS NULL OR a.id < :cursor)
    ORDER BY a.id DESC
""")
  List<AttributeDefinition> findByCursor(UUID cursor, Pageable pageable);

}
