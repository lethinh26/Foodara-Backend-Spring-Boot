package com.db.foodara.repository.store;

import com.db.foodara.entity.store.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionItemRepository extends JpaRepository<OptionItem, String> {
    List<OptionItem> findByOptionGroupIdOrderByDisplayOrder(String optionGroupId);
    List<OptionItem> findByOptionGroupIdInOrderByDisplayOrder(List<String> optionGroupIds);
}
