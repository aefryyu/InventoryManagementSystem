package ims.aefryyu.server.service;

import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.TransactionRequest;
import ims.aefryyu.server.enums.TransactionStatus;

import java.util.UUID;

public interface TransactionService {
    Response restockInventory(TransactionRequest transactionRequest);
    Response sell(TransactionRequest transactionRequest);
    Response returnToSupplier(TransactionRequest transactionRequest);
    Response getAllTransaction(int page, int size, String searchText);
    Response getTransactionById(UUID id);
    Response getTransactionByMonthAndYear(int month, int year);
    Response updateTransactionStatus(UUID transactionId, TransactionStatus transactionStatus);
}
