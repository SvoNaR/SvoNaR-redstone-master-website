package ru.redstonemaster.web.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

	Optional<PendingRegistration> findByVerificationToken(String verificationToken);

	Optional<PendingRegistration> findByUsernameIgnoreCase(String username);

	Optional<PendingRegistration> findByEmailIgnoreCase(String email);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	@Modifying
	@Query("delete from PendingRegistration p where p.verificationExpiresAt < :cutoff")
	void deleteExpired(@Param("cutoff") Instant cutoff);
}
