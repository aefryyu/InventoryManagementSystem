package ims.aefryyu.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import ims.aefryyu.server.enums.TransactionStatus;
import ims.aefryyu.server.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDTO {

    private UUID id;

    private Integer totalProduct;

    private BigDecimal totalPrice;

    private TransactionType transactionType;

    private TransactionStatus transactionStatus;

    private String description;

    private UserDTO user;

    private ProductDTO product;

    private SupplierDTO supplier;

    private Instant updatedAt;

    private Instant createdAt;

}
