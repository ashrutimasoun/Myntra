package com.upgrad.myntra.service.business;


import com.upgrad.myntra.service.dao.CategoryDao;
import com.upgrad.myntra.service.entity.BrandCategoryEntity;
import com.upgrad.myntra.service.entity.CategoryEntity;
import com.upgrad.myntra.service.exception.CategoryNotFoundException;
import com.upgrad.myntra.service.util.MyntraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryDao categoryDao;



    /**
     * The method implements the business logic for getting categories for any particular brand.
     */


    @Override
    public List<CategoryEntity> getCategoriesByBrand(String brandId)  {

        return categoryDao.getCategoriesByBrand(brandId);

    }

    /**
     * The method implements the business logic for getting category by its id endpoint.
     */


    @Override
    public CategoryEntity getCategoryById(final String categoryId) throws CategoryNotFoundException {

        if(categoryId.length() == 0){
            throw new CategoryNotFoundException("CNF-001","Category id field should not be empty");
        }else{
            CategoryEntity categoryEntity = categoryDao.getCategoryById(categoryId);
            if(categoryEntity == null){
                throw new CategoryNotFoundException("CNF-002","No category by this id");
            }

            return categoryEntity;
        }
    }


    /**
     * The method implements the business logic for getting all categories ordered by their name endpoint.
     */


    @Override
    public List<CategoryEntity> getAllCategoriesOrderedByName(){

        return categoryDao.getAllCategoriesOrderedByName();
    }



}
