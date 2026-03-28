package ims.aefryyu.server.service;

import ims.aefryyu.server.dto.CategoryDTO;
import ims.aefryyu.server.dto.Response;

import java.util.UUID;

public interface CategoryService {

    Response createCategory(CategoryDTO categoryDTO);
    Response getAllCategories();
    Response getCategoryById(UUID id);
    Response updateCategory(UUID id, CategoryDTO categoryDTO);
    Response deleteCategory(UUID id);
}
