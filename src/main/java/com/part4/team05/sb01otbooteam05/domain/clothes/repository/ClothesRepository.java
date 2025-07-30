package com.part4.team05.sb01otbooteam05.domain.clothes.repository;

import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID> {

  @EntityGraph(attributePaths = {"attributeValues"})
  List<Clothes> findByOwnerId(UUID ownerId);

  @Query("""
    SELECT c FROM Clothes c
    WHERE c.ownerId = :ownerId
      AND (:cursor IS NULL OR c.id < :cursor)
      AND (:type IS NULL OR c.type = :type)
    ORDER BY c.id DESC
""")
  List<Clothes> findByOwnerIdPageNation(
      @Param("ownerId") UUID ownerId,
      @Param("cursor") UUID cursor,
      @Param("type") ClothesType type,
      Pageable pageable);
}

