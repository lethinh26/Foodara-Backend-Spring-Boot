package com.db.foodara.entity.store;

import java.io.Serializable;
import java.util.Objects;

public class StoreStoreCategoryId implements Serializable {
    private String storeId;
    private String categoryId;

    public StoreStoreCategoryId() {}

    public StoreStoreCategoryId(String storeId, String categoryId) {
        this.storeId = storeId;
        this.categoryId = categoryId;
    }

    public String getStoreId() { return storeId; }
    public String getCategoryId() { return categoryId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreStoreCategoryId that = (StoreStoreCategoryId) o;
        return Objects.equals(storeId, that.storeId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, categoryId);
    }
}
