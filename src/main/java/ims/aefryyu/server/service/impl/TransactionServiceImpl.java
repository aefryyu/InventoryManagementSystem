package ims.aefryyu.server.service.impl;

import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.TransactionDTO;
import ims.aefryyu.server.dto.TransactionRequest;
import ims.aefryyu.server.entity.Product;
import ims.aefryyu.server.entity.Supplier;
import ims.aefryyu.server.entity.Transaction;
import ims.aefryyu.server.entity.User;
import ims.aefryyu.server.enums.TransactionStatus;
import ims.aefryyu.server.enums.TransactionType;
import ims.aefryyu.server.exception.NameValueRequireException;
import ims.aefryyu.server.exception.NotFoundException;
import ims.aefryyu.server.repository.ProductRepository;
import ims.aefryyu.server.repository.SupplierRepository;
import ims.aefryyu.server.repository.TransactionRepository;
import ims.aefryyu.server.service.TransactionService;
import ims.aefryyu.server.service.UserService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final RateLimiterService rateLimiterService;

    @Transactional
    @Override
    public Response restockInventory(TransactionRequest transactionRequest) {

        String key = "txn:" + userService.getCurrentLoggedUser().getId();

        Bucket bucket = rateLimiterService.resolveBucket(key);

        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Too many transaction requests");
        }

        UUID productId = transactionRequest.getProductId();
        UUID supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) {
            throw new NameValueRequireException("Supplier id is required");
        }

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new NotFoundException("Product id not found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier id not found"));

        User user = userService.getCurrentLoggedUser();

        //update stock quantity and re-save
        product.setStockQuantity( (product.getStockQuantity() + quantity));
        productRepository.save(product);

        //create transaction
        Transaction newTransaction = Transaction.builder()
                .transactionType(TransactionType.PURCHASE)
                .transactionStatus(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProduct(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .build();

        transactionRepository.save(newTransaction);

        return Response.builder()
                .status(200)
                .message("Transaction made successfully")
                .build();
    }

    @Transactional
    @Override
    public Response sell(TransactionRequest transactionRequest) {

        UUID productId = transactionRequest.getProductId();
        Integer quantity = transactionRequest.getQuantity();

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new NotFoundException("Product ID not found"));

        if(product.getStockQuantity() < quantity){
            throw new RuntimeException("Insufficient stock");
        }

        User user = userService.getCurrentLoggedUser();

        //update stock and re-save
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        //create a data transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SALE)
                .transactionStatus(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .totalProduct(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message("Sell Transaction Request made successfully")
                .build();
    }

    @Transactional
    @Override
    public Response returnToSupplier(TransactionRequest transactionRequest) {

        UUID productId = transactionRequest.getProductId();
        UUID supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null){
            throw new NameValueRequireException("Supplier name is required");
        }

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new NotFoundException("product not found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier not found"));

        User user = userService.getCurrentLoggedUser();

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .transactionStatus(TransactionStatus.PROCESSING)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProduct(quantity)
                .totalPrice(BigDecimal.ZERO)
                .description(transactionRequest.getDescription())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message("Return Transaction To Supplier Completed")
                .build();
    }

    @Override
    public Response getAllTransaction(int page, int size, String searchText) {
        Pageable pageable =  PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Transaction> transactionPage =  transactionRepository.searchTransactions(searchText, pageable);

        List<TransactionDTO> transactionDTOList = modelMapper
                .map(transactionPage.getContent(), new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOList.forEach(tDto -> {
            tDto.setUser(null);
            tDto.setSupplier(null);
            tDto.setProduct(null);
        });

        return Response.builder()
                .status(200)
                .message("Fetch All Transaction")
                .transactions(transactionDTOList)
                .build();
    }

    @Override
    public Response getTransactionById(UUID id) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id Transaction Not found"));

        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);

        transactionDTO.getUser().setTransactions(null);

        return Response.builder()
                .status(200)
                .message("Fetch transaction ID: " + id)
                .transaction(transactionDTO)
                .build();
    }

    @Override
    public Response getTransactionByMonthAndYear(int month, int year) {

        List<Transaction> transactions = transactionRepository.findByMonthAndYear(month, year);

        List<TransactionDTO> transactionDTOList = modelMapper
                .map(transactions, new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOList.forEach(tDto -> {
            tDto.setUser(null);
            tDto.setProduct(null);
            tDto.setSupplier(null);
        });

        return Response.builder()
                .status(200)
                .message("Fetch Transaction By Month and Year")
                .transactions(transactionDTOList)
                .build();
    }

    @Transactional
    @Override
    public Response updateTransactionStatus(UUID transactionId, TransactionStatus transactionStatus) {

        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction id not found"));

        existingTransaction.setTransactionStatus(transactionStatus);
        existingTransaction.setUpdatedAt(Instant.from(LocalDateTime.now()));

        return Response.builder()
                .status(200)
                .message("Change status transaction successfully")
                .build();
    }
}
