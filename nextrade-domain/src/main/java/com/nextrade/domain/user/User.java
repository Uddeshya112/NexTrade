package com.nextrade.domain.user;
import com.nextrade.common.enumtype.*; import com.nextrade.common.event.UserLockedEvent; import com.nextrade.common.identifier.*; import com.nextrade.common.util.*; import com.nextrade.domain.shared.*; import java.time.*; import java.util.*;
public final class User extends AbstractAggregateRoot<UserId> {
 private final Username username; private final Email email; private String passwordHash; private UserRole role; private UserStatus status; private boolean emailVerified; private int failedAttempts; private Instant lockedUntil; private String lockReason;
 public User(UserId id,String username,String email,String passwordHash,UserRole role,UserStatus status,boolean verified,int failedAttempts,Instant lockedUntil,Instant createdAt,Instant updatedAt,long revision){super(id,createdAt,updatedAt,revision);this.username=new Username(username);this.email=new Email(email);this.passwordHash=Validation.notBlank(passwordHash,"passwordHash");this.role=Objects.requireNonNull(role);this.status=Objects.requireNonNull(status);this.emailVerified=verified;this.failedAttempts=Math.max(0,failedAttempts);this.lockedUntil=lockedUntil;this.lockReason=null;}
 public static User register(String username,String email,String hash,UserRole role){return new User(UserId.generate(),username,email,hash,role,UserStatus.PENDING_VERIFICATION,false,0,null,Instant.now(),Instant.now(),0);}
 public boolean isLocked(Instant now){return lockedUntil!=null&&lockedUntil.isAfter(now);}
 public boolean loginAllowed(Instant now){return status==UserStatus.ACTIVE&&emailVerified&&!isLocked(now);}
 public void verifyEmail(){if(emailVerified)return;if(status!=UserStatus.PENDING_VERIFICATION)throw new IllegalStateException("Verification not applicable");emailVerified=true;status=UserStatus.ACTIVE;touch();}
 public void changePassword(String hash){passwordHash=Validation.notBlank(hash,"passwordHash");touch();}
 public void recordFailedLogin(int maxAttempts,Duration lockout,Instant now){failedAttempts++;if(failedAttempts>=maxAttempts){lockedUntil=now.plus(lockout);lockReason="Too many failed logins";emit(new UserLockedEvent(EventId.generate(),now,id().asString(),lockReason));}touch();}
 public void recordSuccessfulLogin(){failedAttempts=0;lockedUntil=null;lockReason=null;touch();}
 public void suspend(String reason){status=UserStatus.SUSPENDED;lockReason=reason;touch();}
 public void activate(){if(!emailVerified)throw new IllegalStateException("Email must be verified");status=UserStatus.ACTIVE;touch();}
 public Username username(){return username;} public Email email(){return email;} public String passwordHash(){return passwordHash;} public UserRole role(){return role;} public UserStatus status(){return status;} public boolean emailVerified(){return emailVerified;} public int failedAttempts(){return failedAttempts;} public Instant lockedUntil(){return lockedUntil;} public String lockReason(){return lockReason;}
}
