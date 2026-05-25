package com.example.brisa.controllers;

import com.example.brisa.dtos.people.PeopleCreateLinkRequestDTO;
import com.example.brisa.dtos.people.PeopleCreateLinkResponseDTO;
import com.example.brisa.dtos.people.PeopleImportResponseDTO;
import com.example.brisa.dtos.people.PeopleLinkExistingRequestDTO;
import com.example.brisa.dtos.people.PeopleOverviewResponseDTO;
import com.example.brisa.dtos.people.PeopleReferenceDataDTO;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.services.LogHelper;
import com.example.brisa.services.PeopleIntegrationService;
import com.example.brisa.services.PeopleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/people")
@RequiredArgsConstructor
public class PeopleController {

    private final PeopleService peopleService;
    private final PeopleIntegrationService peopleIntegrationService;
    private final LogHelper logHelper;

    @GetMapping
    public ResponseEntity<List<PeopleModel>> getAllPeople() {
        return ResponseEntity.ok(peopleService.findAll());
    }

    @GetMapping("/overview")
    public ResponseEntity<PeopleOverviewResponseDTO> getPeopleOverview(
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String quota,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String institution
    ) {
        return ResponseEntity.ok(
                peopleIntegrationService.getOverview(
                        tab,
                        search,
                        programId,
                        stageId,
                        status,
                        quota,
                        gender,
                        state,
                        city,
                        institution
                )
        );
    }

    @GetMapping("/reference-data")
    public ResponseEntity<PeopleReferenceDataDTO> getReferenceData() {
        return ResponseEntity.ok(peopleIntegrationService.getReferenceData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeopleModel> getPeopleById(@PathVariable Long id) {
        return ResponseEntity.ok(peopleService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PeopleModel> createPeople(
            @RequestBody PeopleModel people,
            HttpServletRequest request
    ) {
        PeopleModel createdPeople = peopleService.create(people);

        try {
            UUID userId = getUserId();
            logHelper.logCreate("People", createdPeople.getId().toString(), createdPeople.getName(), userId, request);
        } catch (Exception ignored) {
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPeople);
    }

    @PostMapping("/link")
    public ResponseEntity<PeopleCreateLinkResponseDTO> createOrLinkPeople(
            @RequestBody PeopleCreateLinkRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        PeopleCreateLinkResponseDTO response = peopleIntegrationService.createOrLink(request);

        try {
            UUID userId = getUserId();
            if (response.pessoaCriada()) {
                logHelper.logCreate("People", response.peopleId().toString(), response.pessoa().nome(), userId, httpRequest);
            } else {
                logHelper.logUpdate("People", response.peopleId().toString(), response.pessoa().nome(), userId, httpRequest);
            }
        } catch (Exception ignored) {
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/create-only")
    public ResponseEntity<PeopleCreateLinkResponseDTO> createOnlyPeople(
            @RequestBody PeopleCreateLinkRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        PeopleCreateLinkResponseDTO response = peopleIntegrationService.createOnly(request);

        try {
            UUID userId = getUserId();
            if (response.pessoaCriada()) {
                logHelper.logCreate("People", response.peopleId().toString(), response.pessoa().nome(), userId, httpRequest);
            } else {
                logHelper.logUpdate("People", response.peopleId().toString(), response.pessoa().nome(), userId, httpRequest);
            }
        } catch (Exception ignored) {
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/link-existing")
    public ResponseEntity<PeopleCreateLinkResponseDTO> linkExistingPerson(
            @RequestBody PeopleLinkExistingRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        PeopleCreateLinkResponseDTO response = peopleIntegrationService.linkExistingPerson(request);

        try {
            UUID userId = getUserId();
            logHelper.logUpdate("People", response.peopleId().toString(), response.pessoa().nome(), userId, httpRequest);
        } catch (Exception ignored) {
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeopleModel> updatePeople(
            @PathVariable Long id,
            @RequestBody PeopleModel people,
            HttpServletRequest request
    ) {
        PeopleModel updatedPeople = peopleService.update(id, people);

        try {
            UUID userId = getUserId();
            logHelper.logUpdate("People", updatedPeople.getId().toString(), updatedPeople.getName(), userId, request);
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(updatedPeople);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeople(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        try {
            PeopleModel people = peopleService.findById(id);
            UUID userId = getUserId();
            logHelper.logDelete("People", id.toString(), people.getName(), userId, request);
        } catch (Exception ignored) {
        }

        peopleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/excel")
    public ResponseEntity<PeopleImportResponseDTO> importPeopleFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "programaId", required = false) Long programaId,
            @RequestParam(value = "turmaId", required = false) Long turmaId,
            @RequestParam(value = "etapaId", required = false) Long etapaId,
            @RequestParam(value = "statusInicial", required = false) String statusInicial,
            HttpServletRequest request
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PeopleImportResponseDTO response = peopleService.importPeopleFromExcel(file, programaId, turmaId, etapaId, statusInicial);

            try {
                UUID userId = getUserId();
                logHelper.logImport("People", response.getSuccessfullyInserted(), response.getAlreadyExists(), userId, request);
            } catch (Exception ignored) {
            }

            return ResponseEntity.ok(response);
        } catch (IOException ignored) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private UUID getUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.example.brisa.models.auth.UserModel user) {
                return user.getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
