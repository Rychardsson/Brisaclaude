package com.example.brisa.controllers;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import java.util.Date;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.brisa.dtos.auth.AuthenticationDTO;
import com.example.brisa.dtos.auth.ForgotPasswordDTO;
import com.example.brisa.dtos.auth.LoginResponseDTO;
import com.example.brisa.dtos.auth.RegisterDTO;
import com.example.brisa.dtos.auth.ResetPasswordDTO;
import com.example.brisa.dtos.user.UserResponseDTO;
import com.example.brisa.exceptions.ConflictException;
import com.example.brisa.infra.security.TokenService;
import com.example.brisa.models.auth.EmailVerificationToken;
import com.example.brisa.models.auth.PasswordResetToken;
import com.example.brisa.models.auth.UserModel;
import com.example.brisa.repositories.EmailVerificationTokenRepository;
import com.example.brisa.repositories.PasswordResetTokenRepository;
import com.example.brisa.repositories.UserRepository;
import com.example.brisa.services.AuthService;
import com.example.brisa.services.EmailService;
import com.example.brisa.services.LogHelper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AuthService authService;
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;
    @Autowired
    private LogHelper logHelper;
    @Value("${frontend.url}")
    private String frontendUrl;
    @Value("${backend.url}")
    private String backendUrl;

    @PostMapping("/login")
    public ResponseEntity login(
            @RequestBody @Valid AuthenticationDTO data,
            HttpServletRequest request){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((UserModel) auth.getPrincipal());
        
        try {
            UserModel user = (UserModel) auth.getPrincipal();
            logHelper.logLogin(user.getId(), user.getLogin(), request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterDTO data,
            HttpServletRequest request) {
        try {
            UserModel newUser = authService.registerUser(data);
            
            try {
                logHelper.logAsync(com.example.brisa.enums.LogAction.USER_REGISTER,
                    "Novo usuário registrado: " + newUser.getLogin(),
                    "User", newUser.getId().toString(), newUser.getId(), request);
            } catch (Exception e) {
                // Log error but don't fail the request
            }
            
            UserResponseDTO responseDTO = new UserResponseDTO(
                newUser.getId(),
                newUser.getLogin(),
                newUser.getEmail(),
                newUser.getRole(),
                newUser.isVerifiedEmail(),
                newUser.getGender(),
                newUser.getDateOfBirth(),
                newUser.isEnabled(),
                newUser.getProfilePicture()
            );

            String token = UUID.randomUUID().toString();
            EmailVerificationToken verificationToken = new EmailVerificationToken(token, newUser);
            tokenRepository.save(verificationToken);

            System.out.println("Preparando para enviar e-mail de verificação para: " + newUser.getEmail());
            String verificationUrl = backendUrl + "/auth/verify?token=" + token;

            
            String htmlContent = emailService.loadEmailTemplateVerification(newUser.getLogin(), verificationUrl);
            System.out.println("Template de e-mail carregado com sucesso. Enviando e-mail...");
            // Envia o e-mail
            emailService.sendEmail(newUser.getEmail(), "Verificação de E-mail", htmlContent);
            System.out.println("E-mail de verificação enviado com sucesso para: " + newUser.getEmail());

            return ResponseEntity.ok(responseDTO);
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao carregar o template de e-mail.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }

    @GetMapping("/verify") // rota para verificar o token
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        Optional<EmailVerificationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido.");
        }

        EmailVerificationToken verificationToken = optionalToken.get();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expirado.");
        }

        UserModel user = verificationToken.getUser();
        user.setVerifiedEmail(true);
        userRepository.save(user); 

        tokenRepository.delete(verificationToken);

        return ResponseEntity.ok("E-mail verificado com sucesso. " + frontendUrl + "/auth/login");
    }




    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordDTO dto) {
        UserModel user = (UserModel) userRepository.findByEmail(dto.email());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }

        try {
            // Verificar se já existe um token para este usuário
            Optional<PasswordResetToken> existingToken = resetTokenRepository.findByUser(user);
            if (existingToken.isPresent()) {
                // Excluir token existente
                resetTokenRepository.delete(existingToken.get());
            }
            
            // Criar novo token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            resetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password/confirm?token=" + token;
            String htmlContent = emailService.loadResetPasswordTemplate(user.getLogin(), resetLink);
            
            emailService.sendEmail(user.getEmail(), "Redefinição de Senha", htmlContent);
            return ResponseEntity.ok("E-mail enviado.");
            
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao enviar e-mail de redefinição de senha: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao carregar o template de redefinição de senha: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao processar solicitação de redefinição de senha: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao processar solicitação de redefinição de senha.");
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        Optional<PasswordResetToken> optionalToken = resetTokenRepository.findByToken(dto.token());
        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido.");
        }

        PasswordResetToken token = optionalToken.get();
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expirado.");
        }

        UserModel user = token.getUser();
        user.setPassword(authService.encodePassword(dto.newPassword()));
        userRepository.save(user);
        resetTokenRepository.delete(token);
        System.out.println("Senha redefinida com sucesso");
        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }

}
