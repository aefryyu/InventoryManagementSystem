package ims.aefryyu.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ims.aefryyu.server.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    //generic
    private int status;
    private String message;

    //for login
    private String token;
    private UserRole role;
    private String expirationTime;

    //data output
    private UserDTO user;
    private List<UserDTO> users;

    private CategoryDTO category;
    private List<CategoryDTO> categories;

    private ProductDTO product;
    private List<ProductDTO> products;

    private TransactionDTO transaction;
    private List<TransactionDTO> transactions;

    private SupplierDTO supplier;
    private List<SupplierDTO> suppliers;

    private final LocalDateTime timeStamps = LocalDateTime.now();
}
