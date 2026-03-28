package ims.aefryyu.server.controller;

import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.SupplierDTO;
import ims.aefryyu.server.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping("/store")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> addSupplier(@RequestBody @Valid SupplierDTO supplierDTO){
        return ResponseEntity.ok(supplierService.addSupplier(supplierDTO));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateSupplier(@PathVariable UUID id,@RequestBody @Valid SupplierDTO supplierDTO){
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAll(){
        return ResponseEntity.ok(supplierService.getAllSupplier());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getSupplierById(@PathVariable UUID id){
        return ResponseEntity.ok(supplierService.getSupplierByID(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteSupplier(@PathVariable UUID id){
        return ResponseEntity.ok(supplierService.deleteSupplier(id));
    }
}
