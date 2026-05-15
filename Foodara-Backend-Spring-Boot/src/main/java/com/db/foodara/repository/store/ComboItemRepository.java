package com.db.foodara.repository.store;

import com.db.foodara.entity.store.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, String> {
    void removeById(String id);

    List<ComboItem> findByComboId(String comboId);

    List<ComboItem> findByComboIdIn(List<String> comboIds);

    void removeByComboId(String comboId);
}
