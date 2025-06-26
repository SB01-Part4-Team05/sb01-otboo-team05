package com.part4.team05.sb01otbooteam05.domain.attribute.repository;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<AttributeValue, Long> {

}
