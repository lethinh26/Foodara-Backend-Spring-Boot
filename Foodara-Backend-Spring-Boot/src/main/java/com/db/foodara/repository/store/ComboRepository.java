package com.db.foodara.repository.store;

import com.db.foodara.entity.store.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<Combo, String> {

    List<Combo> findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(String storeId);

    void removeById(String id);
}
