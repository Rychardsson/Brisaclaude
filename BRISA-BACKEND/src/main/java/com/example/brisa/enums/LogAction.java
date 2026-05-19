package com.example.brisa.enums;

public enum LogAction {
    // User actions
    USER_LOGIN,
    USER_LOGOUT,
    USER_REGISTER,
    USER_UPDATE,
    USER_DELETE,
    USER_PASSWORD_RESET,
    USER_EMAIL_VERIFIED,
    
    // People actions
    PEOPLE_CREATE,
    PEOPLE_UPDATE,
    PEOPLE_DELETE,
    PEOPLE_IMPORT,
    
    // Program actions
    PROGRAM_CREATE,
    PROGRAM_UPDATE,
    PROGRAM_DELETE,
    PROGRAM_IMPORT,
    
    // Class actions
    CLASS_CREATE,
    CLASS_UPDATE,
    CLASS_DELETE,
    CLASS_IMPORT,
    
    // Stage actions
    STAGE_CREATE,
    STAGE_UPDATE,
    STAGE_DELETE,
    STAGE_CANDIDATE_ADD,
    STAGE_CANDIDATE_REMOVE,
    STAGE_CANDIDATE_UPDATE,
    STAGE_CANDIDATES_IMPORT,
    
    // Enrollment actions
    ENROLLMENT_CREATE,
    ENROLLMENT_UPDATE,
    ENROLLMENT_DELETE,
    ENROLLMENT_IMPORT,
    
    // Institution actions
    INSTITUTION_CREATE,
    INSTITUTION_UPDATE,
    INSTITUTION_DELETE,
    INSTITUTION_IMPORT,
    
    // System actions
    SYSTEM_ERROR,
    SYSTEM_WARNING,
    SYSTEM_INFO
}
