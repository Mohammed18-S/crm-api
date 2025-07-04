package com.CRM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1/appUser")
public class CrmApplication {

	private final List<Customer> customers = new ArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(CrmApplication.class, args);
	}

				// Email validation pattern
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

				// Customer model
	private static class Customer {
		private String id;
		private String name;
		private String email;
		private LocalDateTime createdAt;

		public Customer(String id, String name, String email, LocalDateTime createdAt) {
			this.id = id;
			this.name = name;
			this.email = email;
			this.createdAt = createdAt;
		}

						// Getters and setters
		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public LocalDateTime getCreatedAt() { return createdAt; }
		public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	}

					// GET all customers
	@GetMapping("/getCustomers")
	public ResponseEntity<List<Customer>> getAllCustomers() {
		return ResponseEntity.ok(customers);
	}

			// POST new customer
	@PostMapping("/createCustomers")
	public ResponseEntity<Customer> createCustomer(@RequestBody CustomerRequest request) {
		          // Validate email
		if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
		}

		               // Check if email already exists
		if (customers.stream().anyMatch(c -> c.getEmail().equals(request.getEmail()))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
		}

		String id = UUID.randomUUID().toString();
		Customer newCustomer = new Customer(
				id,
				request.getName(),
				request.getEmail(),
				LocalDateTime.now()
		);
		customers.add(newCustomer);
		return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
	}

			// PUT update customer
	@PutMapping("/updateCustomers/{id}")
	public ResponseEntity<Customer> updateCustomer(
			@PathVariable String id,
			@RequestBody CustomerRequest request) {
		// Find customer
		Customer customer = customers.stream()
				.filter(c -> c.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

					// Validate email if it's being updated
		if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
			if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
			}
			if (customers.stream().anyMatch(c -> c.getEmail().equals(request.getEmail()))) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
			}
			customer.setEmail(request.getEmail());
		}

				// Update name if provided
		if (request.getName() != null) {
			customer.setName(request.getName());
		}

		return ResponseEntity.ok(customer);
	}

			// DELETE customer
	@DeleteMapping("/removeCustomers/{id}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
		boolean removed = customers.removeIf(c -> c.getId().equals(id));
		if (!removed) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
		}
		return ResponseEntity.noContent().build();
	}

			// Request DTO for POST and PUT
	private static class CustomerRequest {
		private String name;
		private String email;

				// Getters and setters
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
	}
}