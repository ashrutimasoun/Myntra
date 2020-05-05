package com.upgrad.myntra.service.business;



import com.upgrad.myntra.service.dao.BrandDao;
import com.upgrad.myntra.service.dao.CategoryDao;
import com.upgrad.myntra.service.entity.BrandEntity;
import com.upgrad.myntra.service.entity.CategoryEntity;
import com.upgrad.myntra.service.exception.BrandNotFoundException;
import com.upgrad.myntra.service.exception.CategoryNotFoundException;
import com.upgrad.myntra.service.util.MyntraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private CategoryDao categoryDao;

    /**
     * The method implements the business logic for getting brand details by brand uuid.
     */
    @Override
    public BrandEntity brandByUUID(String brandId) throws BrandNotFoundException {

        if (MyntraUtil.isInValid(brandId)) {
            throw new BrandNotFoundException("RNF-002", "Brand id field should not be empty");
        }
        BrandEntity brand = brandDao.brandByUUID(brandId);
        if (brand != null) {
            return brand;
        }

        throw new BrandNotFoundException("RNF-001", "No brand by this id");
    }


    /**
     * The method implements the business logic for getting brands by their category.
     */
    @Override
    public List<BrandEntity> brandByCategory(String categoryId) throws CategoryNotFoundException {

        if (MyntraUtil.isInValid(categoryId)) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }
        CategoryEntity categoryEntity = categoryDao.getCategoryById(categoryId);
        //If there is no category by the uuid entered by the customer
        if (categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }
        return brandDao.brandByCategory(categoryId);
    }

    /**
     * The method implements the business logic for getting brands by brand name.
     */
    @Override
    public List<BrandEntity> brandsByName(String brandName) throws BrandNotFoundException {

        if (MyntraUtil.isInValid(brandName)) {
            throw new BrandNotFoundException("RNF-003", "brand name field should not be empty");
        }

        StringBuilder likebrandName = new StringBuilder();
        likebrandName.append("%").append(brandName).append("%");
        List<BrandEntity> brands = brandDao.brandByName(likebrandName.toString());
        return brands;
    }


    /**
     * The method implements the business logic for getting all brands ordered by their rating.
     */
    @Override
    public List<BrandEntity> brandsByRating() {
        List<BrandEntity> brands = brandDao.brandByRating();
        return brands;

    }

    }

