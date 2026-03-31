package ims.aefryyu.server.service.impl;

import ims.aefryyu.server.dto.CategoryDTO;
import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.entity.Category;
import ims.aefryyu.server.exception.NotFoundException;
import ims.aefryyu.server.repository.CategoryRepository;
import ims.aefryyu.server.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Response createCategory(CategoryDTO categoryDTO) {
        Category categoryToSave = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.save(categoryToSave);

        return Response.builder()
                .status(200)
                .message("Category Created Successfully")
                .build();
    }

    @Override
    public Response getAllCategories() {
        List<Category> categories = categoryRepository.
                findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "id"));

        List<CategoryDTO> categoryDTOs = modelMapper
                .map(categories, new TypeToken<List<CategoryDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("All Categories")
                .categories(categoryDTOs)
                .build();
    }

    @Override
    public Response getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

        return Response.builder()
                .status(200)
                .message("Category Id: " + categoryDTO.getId())
                .category(categoryDTO)
                .build();
    }

    @Transactional
    @Override
    public Response updateCategory(UUID id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category id not found"));

        if(categoryDTO.getName() != null) existingCategory.setName(categoryDTO.getName());
        categoryRepository.save(existingCategory);

        return Response.builder()
                .status(200)
                .message("Update Category Successfully")
                .build();
    }

    @Transactional
    @Override
    public Response deleteCategory(UUID id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("deleted category successfully")
                .build();
    }
}
