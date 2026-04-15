package com.db.foodara.repository.merchant;

import com.db.foodara.entity.merchant.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByOwnerId(String ownerId);
    boolean existsByOwnerId(String ownerId);
}
