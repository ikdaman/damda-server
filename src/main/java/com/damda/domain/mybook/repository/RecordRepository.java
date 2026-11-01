package com.damda.domain.mybook.repository;

import com.damda.domain.mybook.entity.MyBook;
import com.damda.domain.mybook.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<Record, Long> {

    Optional<Record> findByRecordIdAndMyBook(Long recordId, MyBook myBook);
    List<Record> findAllByMyBookOrderByRecordDateDesc(MyBook myBook);
}
