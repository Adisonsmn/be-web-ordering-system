package com.aromasenja.domain.menu.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class MenuPairingId implements Serializable {
    private UUID menuId;
    private UUID pairingMenuId;
}
