package com.nextrade.api.config;
import com.nextrade.common.event.DomainEvent; import com.nextrade.domain.repository.UserRepository; import com.nextrade.service.*; import com.nextrade.service.ports.*; import org.springframework.context.annotation.*; import java.time.*; import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*;
@Configuration public class ServiceConfiguration {
 @Bean public InMemoryUserRepository userRepository(){return new InMemoryUserRepository();}
 @Bean public InMemorySessionRepository sessionRepository(){return new InMemorySessionRepository();}
 @Bean public InMemoryAuthChallengeRepository challengeRepository(){return new InMemoryAuthChallengeRepository();}
 @Bean public PasswordHasher passwordHasher(){return new Argon2PasswordHasher();}
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
 @Bean public RefreshTokenFactory refreshTokenFactory(JwtKeyRing ring){return new JwtTokenService(ring,Duration.ofDays(30));}
 @Bean public EventOutbox eventOutbox(){return e->{};}
 @Bean public AuthService authService(UserRepository u,SessionRepository s,AuthChallengeRepository c,PasswordHasher p,RefreshTokenFactory r,EventOutbox o,AccessTokenIssuer accessTokens,UserClock clock){return new AuthService(u,s,c,p,r,o,accessTokens,clock,LoginPolicy.defaults(),TokenPolicy.defaults());}
 @Bean public InMemoryRateLimitStore rateLimitStore(){return new InMemoryRateLimitStore();}
}
