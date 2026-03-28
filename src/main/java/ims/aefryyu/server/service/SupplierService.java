package ims.aefryyu.server.service;

import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.SupplierDTO;

import java.util.UUID;

public interface SupplierService {
    Response addSupplier(SupplierDTO supplierDTO);
    Response updateSupplier(UUID id, SupplierDTO supplierDTO);
    Response getAllSupplier();
    Response getSupplierByID(UUID id);
    Response deleteSupplier(UUID id);
}
