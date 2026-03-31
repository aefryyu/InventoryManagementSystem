package ims.aefryyu.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import ims.aefryyu.server.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByUsername(String username);

    List<User> findByDeletedAtIsNull(Sort id);

    List<User> findByDeletedAtIsNotNull(Sort id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

}
