package com.upgrad.myntra.service.dao;


import com.upgrad.myntra.service.entity.AddressEntity;
import com.upgrad.myntra.service.entity.CustomerAddressEntity;
import com.upgrad.myntra.service.entity.CustomerEntity;
import com.upgrad.myntra.service.entity.StateEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * AddressDao class provides the database access for all the endpoints in address controller.
 */
@Repository
@Transactional
public class AddressDaoImpl implements AddressDao{

    //When a container of the application(be it a Java EE container or any other custom container like Spring) manages the lifecycle of the Entity Manager, the Entity Manager is said to be Container Managed. The most com.upgrad.myntra.api.common way of acquiring a Container Managed EntityManager is to use @PersistenceContext annotation on an EntityManager attribute.
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public AddressEntity getAddressByUUID(String addressId) {
        try {
            return entityManager.createNamedQuery("getAddressByUUID", AddressEntity.class).setParameter("uuid", addressId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public AddressEntity saveAddress(AddressEntity addressEntity) {
        entityManager.persist(addressEntity);
        return addressEntity;
    }

    @Override
    public CustomerAddressEntity getCustomerByAddress(String addressId) {
        try {
            return entityManager.createNamedQuery("userByAddress", CustomerAddressEntity.class).setParameter("uuid", addressId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity) {
        entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }

    @Override
    public List<AddressEntity> getAllAddress(CustomerEntity customer){
        try {
            return entityManager.createNamedQuery("getAllAddress", AddressEntity.class).setParameter("customer", customer).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        entityManager.remove(addressEntity);
        return addressEntity;
    }

    @Override
    public StateEntity getStateByUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("getStateByUUID", StateEntity.class).setParameter("uuid",uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
