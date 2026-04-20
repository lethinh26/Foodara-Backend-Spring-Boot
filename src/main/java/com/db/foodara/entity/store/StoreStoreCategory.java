package com.db.foodara.entity.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "store_store_categories")
@IdClass(StoreStoreCategoryId.class)
public class StoreStoreCategory {

    @Id
    @Column(name = "store_id")
    private String storeId;

    @Id
    @Column(name = "category_id")
    private String categoryId;
}
