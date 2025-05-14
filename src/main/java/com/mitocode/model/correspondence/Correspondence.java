package com.mitocode.model.correspondence;

import com.mitocode.model.*;
import com.mitocode.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "correspondence")
@Builder
public class Correspondence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCorrespondence;

    private String referenceCode;

    private LocalDateTime filingDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime sendDate;

    @Column
    private LocalDateTime externalDate;

    @Column
    private LocalDateTime resolvedDate;

    private LocalDateTime expireDate;

    @Column(length = 50)
    private String priority;

    @ManyToOne
    @JoinColumn(name = "type_id", referencedColumnName = "id", nullable = false)
    private CorrespondenceType type;

    @ManyToOne
    @JoinColumn(name = "subtype_id", referencedColumnName = "id", nullable = false)
    private CorrespondenceSubtype subtype;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false)
    private CorrespondenceDepartment correspondenceDepartment;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "idUser", nullable = false)
    private User sender;

    private String recipient;

    @Column(nullable = false, length = 70)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @OneToMany(mappedBy = "correspondence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CorrespondenceAttachment> attachments;
}
