package com.db.foodara.repository;

import com.db.foodara.entity.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRepository extends JpaRepository<Example, String> {
    boolean existsByUsername(String username);
}
