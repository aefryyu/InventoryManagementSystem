package ims.aefryyu.server.service;

import ims.aefryyu.server.dto.ProductDTO;
import ims.aefryyu.server.dto.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProductService {
    Response productStore(ProductDTO productDTO, MultipartFile imageFile);
    Response updateProduct(ProductDTO productDTO, MultipartFile imageFile);
    Response getAllProduct();
    Response getProductById(UUID id);
    Response deleteProduct(UUID id);
}
