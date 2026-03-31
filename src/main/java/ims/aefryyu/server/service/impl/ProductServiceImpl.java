package ims.aefryyu.server.service.impl;

import ims.aefryyu.server.dto.ProductDTO;
import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.entity.Category;
import ims.aefryyu.server.entity.Product;
import ims.aefryyu.server.exception.NotFoundException;
import ims.aefryyu.server.repository.CategoryRepository;
import ims.aefryyu.server.repository.ProductRepository;
import ims.aefryyu.server.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-image/";

    @Transactional
    @Override
    public Response productStore(ProductDTO productDTO, MultipartFile imageFile) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category id not Found"));

        Product productToSave = Product.builder()
                .name(productDTO.getName())
                .sku(productDTO.getSku())
                .slug(productDTO.getSlug())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .stockQuantity(productDTO.getStockQuantity())
                .category(category)
                .build();

        if(imageFile != null){
            String imagePath = saveImage(imageFile);
            productToSave.setImageUrl(imagePath);
        }

        productRepository.save(productToSave);
        return Response.builder()
                .status(200)
                .message("created product successfully")
                .build();
    }

    @Transactional
    @Override
    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {
        Product existingProduct = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new NotFoundException("Product not Found"));

        //Set Image if change
        if(imageFile != null && !imageFile.isEmpty()){
            String imagePath = saveImage(imageFile);
            existingProduct.setImageUrl(imagePath);
        }

        //Category Validation
        if(productDTO.getCategoryId() != null){
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category type not found"));

            existingProduct.setCategory(category);
        }

        //Product Name
        if(productDTO.getName() != null && !productDTO.getName().isBlank()){
            existingProduct.setName(productDTO.getName());
        }

        //Product SKU
        if(productDTO.getSku() != null && !productDTO.getSku().isBlank()){
            existingProduct.setSku(productDTO.getSku());
        }

        //Product SLUG
        if(productDTO.getSlug() != null && !productDTO.getSlug().isBlank()){
            existingProduct.setSlug(productDTO.getSlug());
        }

        //Product Description
        if(productDTO.getDescription() != null && !productDTO.getDescription().isBlank()){
            existingProduct.setDescription(productDTO.getDescription());
        }

        //Product Price
        if(productDTO.getPrice() != null && productDTO.getPrice().compareTo(BigDecimal.ZERO) >= 0){
            existingProduct.setPrice(productDTO.getPrice());
        }

        //Product Stock QTY
        if(productDTO.getStockQuantity() != null && productDTO.getStockQuantity() >= 0){
            existingProduct.setStockQuantity(productDTO.getStockQuantity());
        }

        productRepository.save(existingProduct);
        return Response.builder()
                .status(200)
                .message("Update Product successfully")
                .product(productDTO)
                .build();
    }

    @Override
    public Response getAllProduct() {
        List<Product> products = productRepository.findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "id"));

        List<ProductDTO> productDTOs = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Fetch All Product")
                .products(productDTOs)
                .build();
    }

    @Override
    public Response getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product ID Not Found"));

        return Response.builder()
                .status(200)
                .message("Fetch Product By Id: " + product.getId())
                .product(modelMapper.map(product, ProductDTO.class))
                .build();
    }

    @Transactional
    @Override
    public Response deleteProduct(UUID id) {
        productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id no Found"));

        productRepository.deleteById(id);
        return Response.builder()
                .status(200)
                .message("Deleted Product ID: " + id + " Successfully")
                .build();
    }

    private String saveImage(MultipartFile imageFile){

        //validate type file allowed
        if(!imageFile.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("Only image file allowed");
        }

        File directory = new File(IMAGE_DIRECTORY);

        if(!directory.exists()){
            directory.mkdir();
            log.info("File was created");
        }

        //generate unique name
        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

        //get the absolute path to image
        String imagePath = IMAGE_DIRECTORY + uniqueFileName;

        try {
            File desctinationFile = new File(imagePath);
            imageFile.transferTo(desctinationFile); //writing to this folder
        }catch (Exception e){
            throw new IllegalArgumentException("Error occurred while saving image " + e.getMessage());
        }

        return imagePath;
    }
}
