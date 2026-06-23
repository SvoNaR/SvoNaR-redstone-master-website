package ru.redstonemaster.web.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsernameIgnoreCase(String username);

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

	Optional<User> findByEmailVerificationToken(String token);

	Optional<User> findByPendingEmailVerificationToken(String token);

	Optional<User> findByModSyncToken(String modSyncToken);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByPendingEmailIgnoreCaseAndIdNot(String email, Long id);

	List<User> findByRoleOrderByUsernameAsc(UserRole role);

	@Query("""
			SELECT u FROM User u
			WHERE u.role = :role
			  AND (
			    :search = ''
			    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY u.username ASC
			""")
	Page<User> findByRoleAndSearch(
			@Param("role") UserRole role,
			@Param("search") String search,
			Pageable pageable
	);

	@Modifying
	@Query("delete from User u where u.emailVerified = false")
	void deleteByEmailVerifiedFalse();
}
