package com.upgrad.myntra.api.controllers;




import com.upgrad.myntra.service.business.BrandService;
import com.upgrad.myntra.service.business.CategoryService;
import com.upgrad.myntra.service.business.CustomerService;
import com.upgrad.myntra.service.business.ItemService;
import com.upgrad.myntra.service.entity.AddressEntity;
import com.upgrad.myntra.service.entity.BrandEntity;
import com.upgrad.myntra.service.entity.CategoryEntity;
import com.upgrad.myntra.service.entity.ItemEntity;
import com.upgrad.myntra.service.exception.BrandNotFoundException;
import com.upgrad.myntra.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.upgrad.myntra.api.model.*;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/brand") public class BrandController {

	@Autowired private BrandService brandService;

	@Autowired private ItemService itemService;

	@Autowired private CategoryService categoryService;

	@Autowired private CustomerService customerService;

	/**
	 * A controller method to get a Brand details from the database.
	 *
	 * @param BrandId - The uuid of the Brand whose details has to be fetched from the database.
	 * @return - ResponseEntity<BrandDetailsResponse> type object along with Http status OK.
	 * @throws BrandNotFoundException
	 */

	@RequestMapping(method = RequestMethod.GET, path = "/{brand_id}",
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BrandDetailsResponse> BrandById(@PathVariable(name = "brand_id", required = false)
																  String BrandId) throws BrandNotFoundException {
		BrandEntity brandEntity = brandService.brandByUUID(BrandId);

		BrandDetailsResponse brandDetailsResponse = populateBrandDetailsResponse(brandEntity);

		return new ResponseEntity<BrandDetailsResponse>(brandDetailsResponse, HttpStatus.OK);

	}


	/**
	 * A controller method to get all Brand by a category from the database.
	 *
	 * @param categoryId - The uuid of the category under which the Brand list has to be fetched from the database.
	 * @return - ResponseEntity<BrandListResponse> type object along with Http status OK.
	 * @throws CategoryNotFoundException
	 */

	@RequestMapping(method = RequestMethod.GET, path = "/category/{category_id}",
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BrandListResponse> brandByCategory(@PathVariable(name = "category_id", required = false)
																	 String categoryId) throws CategoryNotFoundException {
		List<BrandEntity> brandListByCategory = brandService.brandByCategory(categoryId);

		return populateBrandList(brandListByCategory);
	}

	/**
	 * A controller method to get all Brand from the database.
	 *
	 * @return - ResponseEntity<BrandListResponse> type object along with Http status OK.
	 */

	

	private BrandDetailsResponse populateBrandDetailsResponse(BrandEntity brandEntity) {

		AddressEntity brandAddress = brandEntity.getAddress();

		BrandDetailsResponseAddress responseAddress = new BrandDetailsResponseAddress();

		responseAddress.id(UUID.fromString(brandAddress.getUuid())).flatBuildingName(brandAddress.getFlatBuilNo())
				.locality(brandAddress.getLocality()).city(brandAddress.getCity()).pincode(brandAddress.getPincode());


		BrandDetailsResponseAddressState state = new BrandDetailsResponseAddressState();
		state.id(UUID.fromString(brandAddress.getState().getUuid())).stateName(brandAddress.getState().getStateName());
		responseAddress.state(state);

		BrandDetailsResponse brandDetailsResponse = new BrandDetailsResponse();

		brandDetailsResponse.id(UUID.fromString(brandEntity.getUuid())).brandName(brandEntity.getbrandName())
				.address(responseAddress).customerRating(BigDecimal.valueOf(brandEntity.getCustomerRating()))
				.numberCustomersRated(brandEntity.getNumberCustomersRated());


		List<CategoryEntity> brandCategories = categoryService.getCategoriesByBrand(brandEntity.getUuid());


		List<CategoryList> categoryListArrayList = new ArrayList<>();
		for (CategoryEntity category : brandCategories) {

			CategoryList categoryList = new CategoryList();

			categoryList.id(UUID.fromString(category.getUuid())).categoryName(category.getCategoryName());

			List<ItemEntity> itemEntities = itemService.getItemsByCategoryAndBrand(brandEntity.getUuid(), category.getUuid());

			List<ItemList> itemListArrayList = new ArrayList<>();
			for (ItemEntity itemEntity : itemEntities) {
				ItemList itemList = new ItemList();
				itemList.id(UUID.fromString(itemEntity.getUuid())).itemName(itemEntity.getItemName()).price(itemEntity.getPrice());
				itemListArrayList.add(itemList);
			}
			categoryList.itemList(itemListArrayList);

			categoryListArrayList.add(categoryList);
		}
		brandDetailsResponse.categories(categoryListArrayList);

		return brandDetailsResponse;
	}

	private ResponseEntity<BrandListResponse> populateBrandList(List<BrandEntity> brandEntityList) {

		List<BrandList> brandsList = new ArrayList<BrandList>();


		if (brandEntityList == null || brandEntityList.isEmpty()) {
			BrandListResponse response = new BrandListResponse();
			response.setBrands(brandsList);
			return new ResponseEntity<BrandListResponse>(response, HttpStatus.OK);

		}

		for (BrandEntity brandEntity : brandEntityList) {
			BrandDetailsResponseAddress responseAddress = new BrandDetailsResponseAddress();

			brandEntity.setCategories(categoryService.getCategoriesByBrand(brandEntity.getUuid()));


			AddressEntity brandAddress = brandEntity.getAddress();
			responseAddress.id(UUID.fromString(brandAddress.getUuid())).flatBuildingName(brandAddress.getFlatBuilNo())
					.locality(brandAddress.getLocality()).city(brandAddress.getCity()).pincode(brandAddress.getPincode());


			BrandDetailsResponseAddressState state = new BrandDetailsResponseAddressState();
			state.id(UUID.fromString(brandAddress.getState().getUuid())).stateName(brandAddress.getState().getStateName());
			responseAddress.state(state);

			BrandList brandList = new BrandList();

			brandList.id(UUID.fromString(brandEntity.getUuid())).brandName(brandEntity.getbrandName())
					.address(responseAddress).customerRating(BigDecimal.valueOf(brandEntity.getCustomerRating()))
					.numberCustomersRated(brandEntity.getNumberCustomersRated());

			List<CategoryEntity> brandCategories = brandEntity.getCategories();
			StringBuilder sb = new StringBuilder();

			for (int index = 0; index < brandCategories.size(); index++) {
				sb.append(brandCategories.get(index).getCategoryName());
				if (index < brandCategories.size() - 1) {
					sb.append(",").append(" ");
				}
			}
			brandList.categories(sb.toString());
			brandsList.add(brandList);
		}
		BrandListResponse response = new BrandListResponse();
		response.setBrands(brandsList);
		return new ResponseEntity<BrandListResponse>(response, HttpStatus.OK);
	}

}
