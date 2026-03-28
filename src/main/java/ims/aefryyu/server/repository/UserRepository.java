package ims.aefryyu.server.repository;

import ims.aefryyu.server.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByDeletedAtIsNull(Sort id);

    List<User> findByDeletedAtIsNotNull(Sort id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
