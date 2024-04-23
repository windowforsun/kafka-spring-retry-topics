package com.windowforsun.kafka.retrytopic.repository;

import com.windowforsun.kafka.retrytopic.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

}
