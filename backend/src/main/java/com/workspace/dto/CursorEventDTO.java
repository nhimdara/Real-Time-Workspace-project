package com.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorEventDTO {
    private UUID userId;
    private String userName;
    private String userAvatar;
    private double x;
    private double y;
    private UUID activeBlockId;
    private String color;
}
