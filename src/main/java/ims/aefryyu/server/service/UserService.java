package ims.aefryyu.server.service;

import ims.aefryyu.server.dto.LoginRequest;
import ims.aefryyu.server.dto.RegisterRequest;
import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.UserDTO;
import ims.aefryyu.server.entity.User;

import java.util.UUID;

public interface UserService {

    Response userRegister(RegisterRequest registerRequest);
    Response userLogin(LoginRequest loginRequest);

    Response getAllUsers();

    User getCurrentLoggedUser();

    Response updateUser(UUID id, UserDTO userDTO);

    Response deleteUser(UUID id);
    Response getAllUsersActive();
    Response getAllUsersInactive();

    Response getUserTransactions(UUID id);
}
