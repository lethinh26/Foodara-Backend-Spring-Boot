package com.db.foodara.entity.order;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @NotNull
    @Column(name = "option_item_id")
    private Long optionItemId;

    @NotBlank
    @Column(name = "option_group_name")
    private String optionGroupName;

    @NotBlank
    @Column(name = "option_name")
    private String optionName;

    @NotNull
    @Column(name = "price_adjustment")
    private BigDecimal priceAdjustment;
}