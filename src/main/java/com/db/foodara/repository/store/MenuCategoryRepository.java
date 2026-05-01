package com.db.foodara.repository.store;

import com.db.foodara.entity.store.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, String> {
    Optional<MenuCategory> findMenuCategoryByStoreIdAndName(String storeId, String name);

    List<MenuCategory> findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(String storeId);

    Optional<MenuCategory> findMenuCategoryById(String id);
    List<MenuCategory> getMenuCategoriesByStoreId(String storeId);

    MenuCategory removeById(String id);
}
