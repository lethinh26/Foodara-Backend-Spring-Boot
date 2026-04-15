package com.db.foodara.repository.store;

import com.db.foodara.entity.store.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroup, String> {
    List<OptionGroup> findByStoreIdOrderByDisplayOrder(String storeId);
}
