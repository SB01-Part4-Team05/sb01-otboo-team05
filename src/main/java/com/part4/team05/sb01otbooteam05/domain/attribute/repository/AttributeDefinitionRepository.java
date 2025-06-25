package com.part4.team05.sb01otbooteam05.domain.attribute.repository;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeDefinition;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {

  Optional<AttributeDefinition> findByName(String name);
}
