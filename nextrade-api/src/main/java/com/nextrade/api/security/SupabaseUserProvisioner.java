package com.nextrade.api.security;

import com.nextrade.common.enumtype.UserStatus;
import com.nextrade.common.identifier.UserId;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.domain.repository.SupabaseIdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class SupabaseUserProvisioner {
    private final UserRepository users;
    private final SupabaseIdentityRepository identities;

    public SupabaseUserProvisioner(UserRepository users, SupabaseIdentityRepository identities) {
        this.users = users;
        this.identities = identities;
    }

    @Transactional
    public LinkedAccount getNexTradeAccount(UUID supabaseUserId, String email) {
        UserId linkedId = identities.findUserId(supabaseUserId).orElse(null);
        if (linkedId != null) return account(requireActive(linkedId));

        User user = users.findByEmail(email.strip().toLowerCase(Locale.ROOT)).orElse(null);
        if (user == null) {
            String username = "sb_" + supabaseUserId.toString().replace("-", "").substring(0, 24);
            user = User.provisionFromSupabase(username, email, "supabase-managed-account");
        } else if (!user.emailVerified() && user.status() == UserStatus.PENDING_VERIFICATION) {
            user.verifyEmail();
        }
        if (user.status() != UserStatus.ACTIVE || !user.emailVerified()) {
            throw new IllegalArgumentException("This NexTrade account is not active");
        }
        User saved = users.save(user);
        identities.link(supabaseUserId, saved.id());
        return account(saved);
    }

    private User requireActive(UserId id) {
        User user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Linked NexTrade account was not found"));
        if (user.status() != UserStatus.ACTIVE || !user.emailVerified()) {
            throw new IllegalArgumentException("This NexTrade account is not active");
        }
        return user;
    }

    private static LinkedAccount account(User user) { return new LinkedAccount(user.id().asString(), user.role().name()); }

    public record LinkedAccount(String nextradeUserId, String role) { }
}
