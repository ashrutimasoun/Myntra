package com.upgrad.myntra.service.business;


import com.upgrad.myntra.service.dao.CustomerDao;
import com.upgrad.myntra.service.entity.CustomerAuthEntity;
import com.upgrad.myntra.service.entity.CustomerEntity;
import com.upgrad.myntra.service.exception.AuthenticationFailedException;
import com.upgrad.myntra.service.exception.AuthorizationFailedException;
import com.upgrad.myntra.service.exception.SignUpRestrictedException;
import com.upgrad.myntra.service.exception.UpdateCustomerException;
import com.upgrad.myntra.service.util.MyntraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     * The method implements the business logic for saving customer details endpoint.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {
        if (MyntraUtil.isInValidEmail(customerEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }

        // Check if the contact number is 10 digits or not
        if (MyntraUtil.isInValidContactNumber(customerEntity.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }

        // Check for Strong Password format
        if (!MyntraUtil.isStrongPassword(customerEntity.getPassword())) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        }

        // Check if the same contact number is already registered
        if (customerDao.getCustomerByContactNumber(customerEntity.getContactNumber()) != null) {
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        }

        // Generate random uuid
        customerEntity.setUuid(UUID.randomUUID().toString());
        // Generate encrypted password and store the Customer details in Database
        String password = customerEntity.getPassword();
        String[] encryptedText = passwordCryptographyProvider.encrypt(password);
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);
        customerEntity = customerDao.saveCustomer(customerEntity);
        return customerEntity;
    }

    /**
     * The method implements the business logic for signin endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String contactNumber, String password) throws AuthenticationFailedException {
        if (MyntraUtil.isInValid(contactNumber) || MyntraUtil.isInValid(password)) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        CustomerEntity customer = customerDao.getCustomerByContactNumber(contactNumber);

        // If no record is found with the given Contact Number, throw error message
        if (customer == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        final String encryptedPassword = passwordCryptographyProvider.encrypt(password, customer.getSalt());
        // Check if the password matches with the encrypted password stored in Database
        if (encryptedPassword.equals(customer.getPassword())) {
            // Generate jwt token based on the password and store in Database
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuth = new CustomerAuthEntity();
            customerAuth.setCustomer(customer);
            customerAuth.setUuid(UUID.randomUUID().toString());
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuth.setAccessToken(jwtTokenProvider.generateToken(customer.getUuid(), now, expiresAt));
            customerAuth.setLoginAt(now);
            customerAuth.setExpiresAt(expiresAt);
            return customerDao.createCustomerAuth(customerAuth);
        } else {
            // Throw Exception if the credentials doesn't match with the Database records
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }
    }

    /**
     * The method implements the business logic for logout endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(String access_token) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuth = authorization(access_token);
        final ZonedDateTime logoutAt = ZonedDateTime.now();
        customerAuth.setLogoutAt(logoutAt);
        customerDao.updateCustomerAuth(customerAuth);
        return customerAuth;
    }

    /**
     * The method implements the business logic for checking authorization of any customer.
     * @return
     */
    @Override
    public CustomerAuthEntity authorization(String access_token) throws AuthorizationFailedException {

        if (access_token != null) {
            CustomerAuthEntity customerAuth = customerDao.getCustomerAuthByAccesstoken(access_token);
            // Token is not matched with the database records
            if (customerAuth == null) {
                throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
            }
            // Customer Already Logged out
            if (customerAuth.getLogoutAt() != null) {
                throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
            }

            // Validating Session Expiry is with in 8 hours or not
            if (!isUserSessionValid(customerAuth.getExpiresAt())) {
                throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
            }
            return customerAuth;
        } else {
            // If the access token is not a valid string to validate
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
    }

    /**
     * The method implements the business logic for updating customer password endpoint.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(String oldPassword, String newPassword, CustomerEntity customerEntity) throws UpdateCustomerException {

        if (!MyntraUtil.isStrongPassword(newPassword)) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        final String encryptedPassword = passwordCryptographyProvider.encrypt(oldPassword, customerEntity.getSalt());
        // Check if old encrypted password matches with Database records
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            final String[] encryptedNewPassword = passwordCryptographyProvider.encrypt(newPassword);
            // Update the new password and salt in database
            customerEntity.setSalt(encryptedNewPassword[0]);
            customerEntity.setPassword(encryptedNewPassword[1]);
            return customerDao.updateCustomer(customerEntity);
        }
        // If Old Password Doesn't match the password in database
        throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
    }




    public Boolean isUserSessionValid(ZonedDateTime expiryTime) {
        if (expiryTime != null) {
            Long timeDifference = ChronoUnit.MILLIS.between(ZonedDateTime.now(), expiryTime);
            // Negative timeDifference indicates an expired access token,
            // difference should be with in the limit, token will be expired after 8 hours
            return (timeDifference >= 0 && timeDifference <= MyntraUtil.EIGHT_HOURS_IN_MILLIS);
        }
        // Token expired or customer never signed in before(may also be the case of invalid token)
        return false;
    }

    /**
     * The method implements the business logic for getting customer details by access token.
     */
    @Override
    public CustomerEntity getCustomer(String access_token) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuth = authorization(access_token);
        return customerAuth.getCustomer();
    }
}

