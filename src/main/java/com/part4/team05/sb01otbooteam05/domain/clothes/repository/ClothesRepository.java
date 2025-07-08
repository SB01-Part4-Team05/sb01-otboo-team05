package com.part4.team05.sb01otbooteam05.domain.clothes.repository;

import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import com.part4.team05.sb01otbooteam05.domain.clothes.entity.ClothesType;
import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID> {

  List<Clothes> findByOwnerId(UUID ownerId);

  @Query("""
    SELECT c FROM Clothes c
    WHERE c.ownerId = :ownerId
      AND (:cursor IS NULL OR c.id < :cursor)
    ORDER BY c.id DESC
""")
  List<Clothes> findByOwnerIdPageNation(UUID ownerId, UUID cursor ,Pageable pageable);
}
