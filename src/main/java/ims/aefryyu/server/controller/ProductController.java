package ims.aefryyu.server.controller;

import ims.aefryyu.server.dto.ProductDTO;
import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/store")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> createProduct(
            @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "imageFile", required = false)MultipartFile imageFile
            ){
        return ResponseEntity.ok(productService.productStore(productDTO, imageFile));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllProduct(){
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getProductById(@PathVariable UUID id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateProduct(
            @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFIle
    ){
        return ResponseEntity.ok(productService.updateProduct(productDTO, imageFIle));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> delete(@PathVariable UUID id){
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

}
