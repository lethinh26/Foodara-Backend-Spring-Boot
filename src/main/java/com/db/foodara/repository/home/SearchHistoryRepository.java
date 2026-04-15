package com.db.foodara.repository.home;

import com.db.foodara.entity.home.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, String> {

    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByUserId(String userId);
}
