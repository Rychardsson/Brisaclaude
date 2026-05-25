package com.example.brisa.dtos.stage;

import java.util.List;

public record WaitlistConvokeResponseDTO(
        int requestedCount,
        int convokedCount,
        List<String> candidateNames
) {
}

