package com.example.brisa.dtos.people;

import lombok.Data;

@Data
public class PeopleLinkExistingRequestDTO {
    private Long peopleId;
    private Long turmaId;
    private Long etapaId;
    private String statusInicial;
}
