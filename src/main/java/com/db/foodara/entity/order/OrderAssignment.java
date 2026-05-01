package com.db.foodara.entity.order;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "order_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Column(name = "driver_id")
    private Long driverId;

    @NotBlank
    @Column(name = "assignment_type")
    private String assignmentType; // auto, manual

    @NotBlank
    @Column(name = "status")
    private String status; // proposed, accepted, rejected, timeout, cancelled

    @Column(name = "distance_to_store_km")
    private Double distanceToStoreKm;

    @Column(name = "response_deadline")
    private LocalDateTime responseDeadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}