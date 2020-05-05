package com.upgrad.myntra.api.controllers;



import com.upgrad.myntra.service.business.CustomerService;
import com.upgrad.myntra.service.entity.CustomerAuthEntity;
import com.upgrad.myntra.service.entity.CustomerEntity;
import com.upgrad.myntra.service.exception.AuthenticationFailedException;
import com.upgrad.myntra.service.exception.AuthorizationFailedException;
import com.upgrad.myntra.service.exception.SignUpRestrictedException;
import com.upgrad.myntra.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.upgrad.myntra.api.model.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customer")
public class CustomerController{


	@Autowired
	private CustomerService customerService;


	/**
	 * A controller method for customer logout.
	 *
	 * @param authorization - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 */


	@RequestMapping(method = RequestMethod.POST, path = "/logout", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String authorization)
			throws AuthorizationFailedException {

		String accessToken = authorization.split("BearerToken ")[1];

		customerService.logout(accessToken);


		LogoutResponse logoutResponse = new LogoutResponse();
		return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
	}



	/**
	 * A controller method for updating customer password.
	 *
	 * @param updatePasswordRequest - This argument contains all the attributes required to update customer password in the database.
	 * @param authorization         - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 * @throws UpdateCustomerException
	 */

	@CrossOrigin
	@RequestMapping(method = RequestMethod.PUT, path = "/password", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UpdatePasswordResponse> changePassword(
			@RequestBody(required = false) final UpdatePasswordRequest updatePasswordRequest,
			@RequestHeader("authorization") final String authorization)
			throws AuthorizationFailedException, UpdateCustomerException
	{
		if (updatePasswordRequest.getOldPassword().equals("") || updatePasswordRequest.getNewPassword().equals("")) {
			throw new UpdateCustomerException("UCR-003", "No field should be empty");
		}

		String accessToken = authorization.split("Bearer ")[1];
		CustomerEntity customerEntity = customerService.getCustomer(accessToken);

		CustomerEntity updatedCustomerEntity = customerService.updateCustomerPassword(
				updatePasswordRequest.getOldPassword(),
				updatePasswordRequest.getNewPassword(),
				customerEntity
		);

		UpdatePasswordResponse updatePasswordResponse = new UpdatePasswordResponse()
				.id(updatedCustomerEntity.getUuid())
				.status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
		return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);
	}


	/**
	 * A controller method for customer signup.
	 *
	 * @param signupCustomerRequest - This argument contains all the attributes required to store customer details in the database.
	 * @return - ResponseEntity<SignupCustomerResponse> type object along with Http status CREATED.
	 * @throws SignUpRestrictedException
	 */

	@RequestMapping(method = RequestMethod.POST,path = "/signup", consumes= MediaType.APPLICATION_JSON_UTF8_VALUE, produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<SignupCustomerResponse> signup(@RequestBody final SignupCustomerRequest signupCustomerRequest)throws SignUpRestrictedException {



		CustomerEntity customerEntity = new CustomerEntity();
		customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
		customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
		customerEntity.setLastName(signupCustomerRequest.getLastName());
		customerEntity.setFirstName(signupCustomerRequest.getFirstName());
		customerEntity.setPassword(signupCustomerRequest.getPassword());
		customerEntity.setSalt("123456");
		customerEntity.setUuid(UUID.randomUUID().toString());

		final CustomerEntity responseCustomer = customerService.saveCustomer(customerEntity);
		SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse();
		signupCustomerResponse.setId(responseCustomer.getUuid());
		signupCustomerResponse.setStatus("Customer Registered");

		return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);

	}


	/**
	 * A controller method for customer authentication.
	 *
	 * @param authorization - A field in the request header which contains the customer credentials as Basic authentication.
	 * @return - ResponseEntity<LoginResponse> type object along with Http status OK.
	 * @throws AuthenticationFailedException
	 */

	@RequestMapping(method =RequestMethod.POST,path="/login",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization)
			throws AuthenticationFailedException {

		System.out.println("Auth Token " + authorization);

		byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
		String decodedText = new String(decode);


		if (!decodedText.contains(":")) {
			throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
		}

		String decodedArray[] = decodedText.split(":");
		CustomerAuthEntity customerAuthEntity =customerService.authenticate(decodedArray[0], decodedArray[1]);
		CustomerEntity customer = customerAuthEntity.getCustomer();

		//creating login response
		LoginResponse loginResponse=new LoginResponse();

		loginResponse.setId(customer.getUuid());
		loginResponse.setMessage("LOGGED IN SUCCESSFULLY");
		loginResponse.setFirstName(customer.getFirstName());
		loginResponse.setLastName(customer.getLastName());
		loginResponse.setEmailAddress(customer.getEmail());
		loginResponse.setContactNumber(customer.getContactNumber());


		HttpHeaders headers = new HttpHeaders();
		headers.add("accessToken", customerAuthEntity.getAccessToken());

		List<String> header = new ArrayList<>();
		header.add("accessToken");
		headers.setAccessControlAllowHeaders(header);

		return new ResponseEntity<LoginResponse>(loginResponse,headers,HttpStatus.OK);

	}
}
