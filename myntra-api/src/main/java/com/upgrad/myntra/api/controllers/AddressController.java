package com.upgrad.myntra.api.controllers;



import com.upgrad.myntra.api.common.Utility;
import com.upgrad.myntra.service.business.AddressService;
import com.upgrad.myntra.service.business.CustomerService;
import com.upgrad.myntra.service.entity.AddressEntity;
import com.upgrad.myntra.service.entity.CustomerEntity;
import com.upgrad.myntra.service.entity.StateEntity;
import com.upgrad.myntra.service.exception.AddressNotFoundException;
import com.upgrad.myntra.service.exception.AuthorizationFailedException;
import com.upgrad.myntra.service.exception.SaveAddressException;
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
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AddressService addressService;


    /**
     * A controller method to delete an address from the database.
     *
     * @param addressId    - The uuid of the address to be deleted from the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<DeleteAddressResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws AddressNotFoundException
     */

    @DeleteMapping("/{address_id}")
    public ResponseEntity<DeleteAddressResponse> deleteAddress(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("address_id") final String addressId)
            throws AuthorizationFailedException, AddressNotFoundException {

        CustomerEntity customerEntity = customerService.getCustomer(Utility.getTokenFromAuthorizationField(authorization));

        //Call AddressService to search AddressEntity
        AddressEntity addressEntity = addressService.getAddressByUUID(addressId, customerEntity);

        AddressEntity deletedAddressEntity = addressService.deleteAddress(addressEntity);

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse().id(UUID.fromString(deletedAddressEntity.getUuid())).status("ADDRESS DELETED SUCCESSFULLY");

        return new ResponseEntity<>(deleteAddressResponse, HttpStatus.OK);
    }

    /**
     * A controller method to save an address in the database.
     *
     * @body SaveAddressRequest - This argument contains all the attributes required to store address details in the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<SaveAddressResponse> type object along with Http status CREATED.
     * @throws AuthorizationFailedException
     * @throws SaveAddressException
     * @throws AddressNotFoundException
     */

    @RequestMapping(
            method = RequestMethod.POST,
            path = "",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(
            @RequestHeader("authorization") final String authorization,
            @RequestBody(required = false) final SaveAddressRequest saveAddressRequest)
            throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        CustomerEntity customerEntity = customerService.getCustomer(Utility.getTokenFromAuthorizationField(authorization));

        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        AddressEntity addressEntity = new AddressEntity();

        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setState(stateEntity);


        AddressEntity createdAddressEntity = addressService.saveAddress(addressEntity,customerEntity);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(createdAddressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);
    }

    /**
     * A controller method to get all address from the database.
     *
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<AddressListResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     */

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/customer",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAddressList(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {

        CustomerEntity customerEntity = customerService.getCustomer(Utility.getTokenFromAuthorizationField(authorization));

        List<AddressEntity> listAddressEntity = addressService.getAllAddress(customerEntity);

        List<AddressList> listAddressList = null;
        if (listAddressEntity.size() != 0) {
            listAddressList = new ArrayList<AddressList>();
            for (AddressEntity addressEntity : listAddressEntity) {
                AddressListState addressListState = new AddressListState()
                        .id(UUID.fromString(addressEntity.getState().getUuid()))
                        .stateName(addressEntity.getState().getStateName());
                listAddressList.add(new AddressList()
                        .id(UUID.fromString(addressEntity.getUuid()))
                        .flatBuildingName(addressEntity.getFlatBuilNo())
                        .locality(addressEntity.getLocality())
                        .city(addressEntity.getCity())
                        .pincode(addressEntity.getPincode())
                        .state(addressListState));
            }
        }

        AddressListResponse addressListResponse = new AddressListResponse().addresses(listAddressList);

        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }
}
