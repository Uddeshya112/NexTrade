package com.nextrade.api.config;
import com.nextrade.common.event.DomainEvent; import com.nextrade.domain.repository.UserRepository; import com.nextrade.service.*; import com.nextrade.service.ports.*; import org.springframework.context.annotation.*; import java.time.*; import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
@Configuration public class ServiceConfiguration {
 @Bean public InMemorySessionRepository sessionRepository(){return new InMemorySessionRepository();}
 @Bean public InMemoryAuthChallengeRepository challengeRepository(){return new InMemoryAuthChallengeRepository();}
 @Bean public PasswordHasher passwordHasher(){return new Argon2PasswordHasher();}
 @Bean
 @ConditionalOnProperty(name="nextrade.mail.verification-enabled", havingValue="true")
 public VerificationMailer smtpVerificationMailer(JavaMailSender mailSender, @Value("${nextrade.mail.from}") String from,
     @Value("${spring.mail.host}") String host, @Value("${spring.mail.username:}") String username,
     @Value("${spring.mail.password:}") String password){
  if(from.isBlank()||host.isBlank()||host.equalsIgnoreCase("localhost")||username.isBlank()||password.isBlank())
   throw new IllegalStateException("SMTP verification is enabled but sender, host, username, or password is missing");
  return new VerificationMailer(){
   public boolean isConfigured(){return true;}
   public void sendVerification(String email,String username,String selector,String code){
    SimpleMailMessage message=new SimpleMailMessage();
    message.setFrom(from); message.setTo(email); message.setSubject("Verify your NexTrade account");
    message.setText("Hello "+username+",\n\nYour NexTrade verification code is: "+code+"\nVerification reference: "+selector+"\n\nEnter both values in the verification form. This code expires in 24 hours. If you did not create this account, ignore this email.");
    mailSender.send(message);
   }
  };
 }
 @Bean @ConditionalOnMissingBean(VerificationMailer.class)
 public VerificationMailer disabledVerificationMailer(){return new VerificationMailer(){public boolean isConfigured(){return false;} public void sendVerification(String email,String username,String selector,String code){throw new IllegalStateException("SMTP verification is disabled");}};}
 @Bean public UserClock userClock(){return Instant::now;}
 @Bean public JwtKeyRing jwtKeyRing(){
  String raw=System.getenv("JWT_SECRET");
  if(raw==null||raw.isBlank())throw new IllegalStateException("JWT_SECRET must be configured; refusing to use a hard-coded signing key");
  byte[] keyBytes=raw.getBytes(java.nio.charset.StandardCharsets.UTF_8);
  if(keyBytes.length<32)throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
  String kid=Optional.ofNullable(System.getenv("JWT_KEY_ID_CURRENT")).filter(v->!v.isBlank()).orElse("key-1");
  Map<String,String> keys=new HashMap<>();
  keys.put(kid,Base64.getEncoder().encodeToString(keyBytes));
  String previousRaw=System.getenv("JWT_PREVIOUS_SECRET");
  String previousKid=System.getenv("JWT_KEY_ID_PREVIOUS");
  if(previousRaw!=null&&!previousRaw.isBlank()){
   if(previousRaw.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<32)throw new IllegalStateException("JWT_PREVIOUS_SECRET must contain at least 32 UTF-8 bytes");
   if(previousKid==null||previousKid.isBlank()||previousKid.equals(kid))throw new IllegalStateException("JWT_KEY_ID_PREVIOUS must be set and differ from JWT_KEY_ID_CURRENT when JWT_PREVIOUS_SECRET is supplied");
   keys.put(previousKid,Base64.getEncoder().encodeToString(previousRaw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }
  return new JwtKeyRing(kid,keys);
 }
 @Bean public com.nextrade.api.security.AccessTokenService accessTokenService(JwtKeyRing ring){return new com.nextrade.api.security.AccessTokenService(ring,Duration.ofMinutes(15));}
 @Bean public RefreshTokenFactory refreshTokenFactory(JwtKeyRing ring){return new JwtTokenService(ring,Duration.ofDays(30));}
 @Bean public AuthService authService(UserRepository u,SessionRepository s,AuthChallengeRepository c,PasswordHasher p,RefreshTokenFactory r,com.nextrade.domain.repository.EventOutbox o,AccessTokenIssuer accessTokens,UserClock clock,VerificationMailer mailer){return new AuthService(u,s,c,p,r,o,accessTokens,clock,LoginPolicy.defaults(),TokenPolicy.defaults(),mailer);}
 @Bean public InMemoryRateLimitStore rateLimitStore(){return new InMemoryRateLimitStore();}
}
