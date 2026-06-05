// LocationLogId.java
package com.pathkeeper.persister.domain.location;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LocationLogId implements Serializable {
    private Long id;
    private LocalDateTime recordedAt;
}
