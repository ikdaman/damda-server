package com.damda.domain.mybook.repository;

import com.damda.domain.mybook.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {

}
