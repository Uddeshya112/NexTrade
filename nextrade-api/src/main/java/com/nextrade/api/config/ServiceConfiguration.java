package com.nextrade.api.config;
import com.nextrade.common.event.DomainEvent; import com.nextrade.domain.repository.UserRepository; import com.nextrade.service.*; import com.nextrade.service.ports.*; import org.springframework.context.annotation.*; import java.time.*; import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*;
@Configuration public class ServiceConfiguration {
 @Bean public InMemoryUserRepository userRepository(){return new InMemoryUserRepository();}
 @Bean public InMemorySessionRepository sessionRepository(){return new InMemorySessionRepository();}
 @Bean public InMemoryAuthChallengeRepository challengeRepository(){return new InMemoryAuthChallengeRepository();}
 @Bean public PasswordHasher passwordHasher(){return new Argon2PasswordHasher();}
 @Bean public UserClock userClock(){return Instant::now;}
 @Bean public JwtKeyRing jwtKeyRing(){String secret=Base64.getEncoder().encodeToString("NexTrade-dev-key-change-in-production-012345678901234567890123".getBytes(java.nio.charset.StandardCharsets.UTF_8));return new JwtKeyRing("v1",Map.of("v1",secret));}
 @Bean public RefreshTokenFactory refreshTokenFactory(JwtKeyRing ring){return new JwtTokenService(ring,Duration.ofDays(30));}
 @Bean public EventOutbox eventOutbox(){return e->{};}
 @Bean public AuthService authService(UserRepository u,SessionRepository s,AuthChallengeRepository c,PasswordHasher p,RefreshTokenFactory r,EventOutbox o,AccessTokenIssuer accessTokens,UserClock clock){return new AuthService(u,s,c,p,r,o,accessTokens,clock,LoginPolicy.defaults(),TokenPolicy.defaults());}
 @Bean public InMemoryRateLimitStore rateLimitStore(){return new InMemoryRateLimitStore();}
}
