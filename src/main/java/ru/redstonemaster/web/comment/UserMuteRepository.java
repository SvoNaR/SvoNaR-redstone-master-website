package ru.redstonemaster.web.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserMuteRepository extends JpaRepository<UserMute, Long> {

	@Query("""
			SELECT m FROM UserMute m
			WHERE m.userId = :userId AND m.mutedUntil > :now
			ORDER BY m.mutedUntil DESC
			LIMIT 1
			""")
	Optional<UserMute> findActiveMute(@Param("userId") Long userId, @Param("now") Instant now);

	@Query("""
			SELECT m FROM UserMute m
			WHERE m.mutedUntil > :now
			ORDER BY m.mutedUntil ASC
			""")
	List<UserMute> findAllActiveMutes(@Param("now") Instant now);

	@Query("""
			SELECT m FROM UserMute m
			WHERE m.userId = :userId AND m.mutedUntil > :now
			""")
	List<UserMute> findActiveMutesForUser(@Param("userId") Long userId, @Param("now") Instant now);
}
