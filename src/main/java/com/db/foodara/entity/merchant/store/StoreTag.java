package com.db.foodara.entity.merchant.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "store_tag")
public class StoreTag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String slug;
    private String tagType;
    private String iconUrl;
    private String colorHex;

    // freeship / bestseller/ moiws, ....
}
