package com.example.brisa.dtos.stage;

import java.util.List;

public record ApprovedImportResponseDTO(
        int totalProcessed,
        int approvedCount,
        int waitlistCount,
        int rejectedCount,
        int conflictsCount,
        List<String> warnings
) {
}

