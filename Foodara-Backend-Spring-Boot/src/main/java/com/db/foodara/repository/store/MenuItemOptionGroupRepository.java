package com.db.foodara.repository.store;

import com.db.foodara.entity.store.MenuItemOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemOptionGroupRepository extends JpaRepository<MenuItemOptionGroup, String> {
    List<MenuItemOptionGroup> findByMenuItemId(String menuItemId);
    List<MenuItemOptionGroup> findByMenuItemIdIn(List<String> menuItemIds);
}
