package com.db.foodara.repository.store;

import com.db.foodara.entity.store.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, String> {

    List<MenuCategory> findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(String storeId);
}
