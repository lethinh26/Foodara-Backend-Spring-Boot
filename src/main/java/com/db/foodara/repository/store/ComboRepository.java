package com.db.foodara.repository.store;

import com.db.foodara.entity.store.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComboRepository extends JpaRepository<Combo, String> {

    void removeById(String id);
}
