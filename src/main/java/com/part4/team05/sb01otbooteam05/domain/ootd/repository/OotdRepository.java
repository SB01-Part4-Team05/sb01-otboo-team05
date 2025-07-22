package com.part4.team05.sb01otbooteam05.domain.ootd.repository;

import com.part4.team05.sb01otbooteam05.domain.ootd.entity.Ootd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OotdRepository extends JpaRepository<Ootd, UUID> {


}
