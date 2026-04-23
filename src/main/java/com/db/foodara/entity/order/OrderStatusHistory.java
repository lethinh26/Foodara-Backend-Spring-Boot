package com.db.foodara.entity.order;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotBlank
    @Column(name = "from_status")
    private String fromStatus;

    @NotBlank
    @Column(name = "to_status")
    private String toStatus;

    @NotBlank
    @Column(name = "changed_by")
    private String changedBy;

    @NotBlank
    @Column(name = "changed_by_role")
    private String changedByRole;

    @Column(name = "note")
    private String note;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}