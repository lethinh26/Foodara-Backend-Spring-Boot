package com.db.foodara.entity.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Column(name = "menu_item_id")
    private Long menuItemId;

    @Column(name = "combo_id")
    private Long comboId;

    @NotBlank
    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_image_url")
    private String itemImageUrl;

    @Min(1)
    @Column(name = "quantity")
    private Integer quantity;

    @NotNull
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @NotNull
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "options_snapshot", columnDefinition = "jsonb")
    private String optionsSnapshot;

    @Column(name = "special_instructions")
    private String specialInstructions;
}