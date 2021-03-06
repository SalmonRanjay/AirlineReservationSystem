package com.kirilanastasoff.ars.customer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kirilanastasoff.ars.model.customer.Customer;
import com.kirilanastasoff.ars.model.customer.Role;
import com.kirilanastasoff.ars.repository.customer.CustomerRepository;
import com.kirilanastasoff.ars.repository.customer.RoleRepository;

@Service
public class CustomerServiceImpl implements CustomerService, UserDetailsService {

	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	public CustomerServiceImpl() {
		super();
	}

	@Autowired
	public CustomerServiceImpl(CustomerRepository customerRepository, RoleRepository roleRepository,
			BCryptPasswordEncoder encoder) {
		super();
		this.customerRepository = customerRepository;
		this.roleRepository = roleRepository;
		this.encoder = encoder;
	}

	@Override
	public List<Customer> getAllCustomers() {
		return  customerRepository.findAll();
	}

	@Override
	public Optional<Customer> getCustomerById(Long id) {
		return this.customerRepository.findById(id);
	}

	@Override
	public Customer findByEmail(String email) {
		return this.customerRepository.findByEmail(email);
	}

	@Override
	public void deleteCustomerById(Long id) {
		this.customerRepository.deleteById(id);
		
	}

	@Override
	public Customer saveCustomer(Customer customer) {
		Customer tempCustomer = new Customer();
		tempCustomer.setEmail(customer.getEmail());
		tempCustomer.setFirstName(customer.getFirstName());
		tempCustomer.setLastName(customer.getLastName());
		tempCustomer.setPassword(encoder.encode(customer.getPassword()));
		tempCustomer.setUsername(customer.getUsername());
		tempCustomer.setEnabled(true);
		
		Role customerRole = roleRepository.findByRole("ADMIN");
		if(customerRole == null) {
			customerRole = new Role();
			customerRole.setRole("USER");
		}
		roleRepository.save(customerRole);
		
		tempCustomer.setRoles(new HashSet<Role>(Arrays.asList(customerRole)));

		this.customerRepository.save(tempCustomer);
		return tempCustomer;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Customer customer = customerRepository.findByUsername(username);
		if(customer == null) {
			throw new UsernameNotFoundException("Invalid details");
		}
		List<GrantedAuthority> authorities = getCustomerAutority((Set<Role>) customer.getRoles());
		return buildUserForAuthentication(customer, authorities);
	}
	
	private List<GrantedAuthority> getCustomerAutority(Set<Role> role) {
		Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
		for(Role currRole : role) {
			roles.add(new SimpleGrantedAuthority(currRole.getRole()));
		}
		List<GrantedAuthority> grandAuthorities = new ArrayList<>(roles);
		return grandAuthorities;
	}
	
	private UserDetails buildUserForAuthentication(Customer customer, List<GrantedAuthority> authorities) {
		return new User(customer.getEmail(), customer.getPassword(),true, true, true, true, authorities);
	}

	@Override
	public Customer findCustomerByUsername(String username) {
		return this.customerRepository.findByUsername(username);
	}
	
	

}
