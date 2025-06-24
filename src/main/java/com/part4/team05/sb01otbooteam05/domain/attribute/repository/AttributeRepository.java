package com.part4.team05.sb01otbooteam05.domain.attribute.repository;

import com.part4.team05.sb01otbooteam05.domain.attribute.entity.Attribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, UUID> {

}
