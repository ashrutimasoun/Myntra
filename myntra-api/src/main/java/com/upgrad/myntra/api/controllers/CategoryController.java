package com.upgrad.myntra.api.controllers;



import com.upgrad.myntra.service.business.CategoryService;
import com.upgrad.myntra.service.entity.CategoryEntity;
import com.upgrad.myntra.service.entity.ItemEntity;
import com.upgrad.myntra.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.upgrad.myntra.api.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * A controller method to get all address from the database.
     *
     * @param categoryId - The uuid of the category whose detail is asked from the database..
     * @return - ResponseEntity<CategoryDetailsResponse> type object along with Http status OK.
     * @throws CategoryNotFoundException
     */

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/{categoryId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetails(
            @PathVariable("categoryId") final String categoryId)
            throws CategoryNotFoundException {

        if (categoryId == null || categoryId.isEmpty()) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        CategoryEntity categoryEntity = categoryService.getCategoryById(categoryId);

        List<ItemList> listItemList = new ArrayList<ItemList>();
        for (ItemEntity i : categoryEntity.getItems()) {
            listItemList.add(new ItemList()
                    .id(UUID.fromString(i.getUuid()))
                    .itemName(i.getItemName())
                    .price(i.getPrice()));
        }

        CategoryDetailsResponse categoryDetailsResponse = new CategoryDetailsResponse()
                .id(UUID.fromString(categoryEntity.getUuid()))
                .categoryName(categoryEntity.getCategoryName())
                .itemList(listItemList);

        return new ResponseEntity<CategoryDetailsResponse>(categoryDetailsResponse, HttpStatus.OK);
    }


    /**
     * A controller method to get all categories from the database.
     *
     * @return - ResponseEntity<CategoriesListResponse> type object along with Http status OK.
     */

    @RequestMapping(
            method = RequestMethod.GET,
            path = "",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoriesListResponse> getAllCategoriesOrderedByName() {

        List<CategoryEntity> listCategoryEntity = categoryService.getAllCategoriesOrderedByName();

        List<CategoryListResponse> listCategoryListResponse = null;

        if (listCategoryEntity.size() != 0) {

            listCategoryListResponse = new ArrayList<>();

            for (CategoryEntity c : listCategoryEntity) {
                listCategoryListResponse.add(new CategoryListResponse()
                        .id(UUID.fromString(c.getUuid()))
                        .categoryName(c.getCategoryName()));
            }
        }

        CategoriesListResponse categoriesListResponse = new CategoriesListResponse().categories(listCategoryListResponse);
        return new ResponseEntity<>(categoriesListResponse, HttpStatus.OK);
    }
}

