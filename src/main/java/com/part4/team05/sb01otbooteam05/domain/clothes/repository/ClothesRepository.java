package com.part4.team05.sb01otbooteam05.domain.clothes.repository;

import com.part4.team05.sb01otbooteam05.domain.clothes.entity.Clothes;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID> {

}
