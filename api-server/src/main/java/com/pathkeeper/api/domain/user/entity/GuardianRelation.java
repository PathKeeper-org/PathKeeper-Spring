// api-server/src/main/java/com/pathkeeper/api/domain/user/GuardianRelation.java
package com.pathkeeper.api.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "guardian_relations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_guardian_protege",
                        columnNames = {"guardian_id", "protege_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GuardianRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private User guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protege_id", nullable = false)
    private User protege;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public GuardianRelation(User guardian, User protege) {
        validate(guardian, protege);
        this.guardian = guardian;
        this.protege = protege;
    }

    private void validate(User guardian, User protege) {
        if (guardian.equals(protege)) {
            throw new IllegalArgumentException("보호자와 피보호자는 같을 수 없습니다");
        }
        if (!guardian.isGuardian()) {
            throw new IllegalArgumentException("보호자 역할이 아닙니다");
        }
        if (!protege.isProtege()) {
            throw new IllegalArgumentException("피보호자 역할이 아닙니다");
        }
    }
}