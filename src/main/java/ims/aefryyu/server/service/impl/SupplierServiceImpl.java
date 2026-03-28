package ims.aefryyu.server.service.impl;

import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.SupplierDTO;
import ims.aefryyu.server.entity.Supplier;
import ims.aefryyu.server.exception.NotFoundException;
import ims.aefryyu.server.repository.SupplierRepository;
import ims.aefryyu.server.service.SupplierService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Response addSupplier(SupplierDTO supplierDTO) {
        Supplier supplierToSave = modelMapper.map(supplierDTO, Supplier.class);
        supplierRepository.save(supplierToSave);

        return Response.builder()
                .status(200)
                .message("Add supplier successfully")
                .build();
    }

    @Transactional
    @Override
    public Response updateSupplier(UUID id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier id: " + id + " Not found"));

        if(supplierDTO.getName() != null) existingSupplier.setName(supplierDTO.getName());
        if(supplierDTO.getAddress() != null) existingSupplier.setAddress(supplierDTO.getAddress());

        supplierRepository.save(existingSupplier);

        return Response.builder()
                .status(200)
                .message("Edited Supplier successfully")
                .build();
    }

    @Override
    public Response getAllSupplier() {
        List<Supplier> categories = supplierRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<SupplierDTO> supplierDTOs = modelMapper.map(categories, new TypeToken<List<SupplierDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Fetch all supplier")
                .suppliers(supplierDTOs)
                .build();
    }

    @Override
    public Response getSupplierByID(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id : " + id  +" doesn't exist"));
        SupplierDTO supplierDTO = modelMapper.map(supplier, SupplierDTO.class);

        return Response.builder()
                .status(200)
                .message("Fetch id: " + id)
                .supplier(supplierDTO)
                .build();
    }

    @Transactional
    @Override
    public Response deleteSupplier(UUID id) {
        supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id not found"));
        supplierRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("deleted id successfully")
                .build();
    }
}
