package com.db.foodara.repository.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.store.MenuItemOptionGroup;

@Repository
public interface MenuItemOptionGroupRepository extends JpaRepository<MenuItemOptionGroup, String> {
    List<MenuItemOptionGroup> findByMenuItemId(String menuItemId);
    List<MenuItemOptionGroup> findByMenuItemIdIn(List<String> menuItemIds);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByMenuItemId(String menuItemId);
}
