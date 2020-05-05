package com.upgrad.myntra.service.business;



import com.upgrad.myntra.service.dao.ItemDao;
import com.upgrad.myntra.service.entity.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemDao itemDao;

    /**
     * The method implements the business logic for getting list of items based on brand and category uuid.
     */
    @Override
    public List<ItemEntity> getItemsByCategoryAndBrand(String brandId, String categoryId) {

        List<ItemEntity> itemsOfbrand = (List<ItemEntity>) itemDao.getItemsByCategoryAndBrand(brandId,categoryId);
        // Get Items based on category uuid
        List<ItemEntity> categoryItems = itemDao.getItemsByCategoryAndBrand(brandId,categoryId);
        List<ItemEntity> categoryItemsOfBrand = new ArrayList<ItemEntity>();
        if (itemsOfbrand != null) {
            itemsOfbrand.forEach(item -> {
                if (categoryItems != null) {
                    for (ItemEntity categoryItem : categoryItems) {
                        // Check if the item belongs to one of the items in this category
                        if (item.getId() == categoryItem.getId()) {
                            categoryItemsOfBrand.add(item);
                            break;
                        }
                    }
                }
            });
        }
        return categoryItemsOfBrand;

    }

}
