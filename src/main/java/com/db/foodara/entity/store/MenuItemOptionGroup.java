package com.db.foodara.entity.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "menu_item_option_groups")
@Getter
@Setter
public class MenuItemOptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "menu_item_id", nullable = false)
    private String menuItemId;

    @Column(name = "option_group_id", nullable = false)
    private String optionGroupId;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
