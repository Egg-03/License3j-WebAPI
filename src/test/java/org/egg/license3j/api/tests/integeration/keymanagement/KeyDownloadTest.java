package org.egg.license3j.api.tests.integeration.keymanagement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.egg.license3j.api.service.LicenseService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;

import javax0.license3j.io.IOFormat;

@SpringBootTest
@AutoConfigureMockMvc
class KeyDownloadTest {

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	@Autowired
	private MockHttpSession session;

	@Autowired
	private LicenseService ls;

	@BeforeEach
	void setUp() {

		// Create a new session for each test method
		session = new MockHttpSession();

		// Set up MockMvc with the actual web application context
		// RequestContextFilter is crucial for @SessionScope to work correctly in tests
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(new RequestContextFilter()).build();
		// Ensures session scope is properly handled
	}

	@Test
	void downloadGeneratedKeys() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", "RSA/ECB/PKCS1Padding")
				.param("size", String.valueOf(1024))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Keys have been generated in memory. Download and save them to a secure location if you plan to use them for signing a license")));
		
		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		assertTrue(ls.isPrivateKeyLoaded());
		assertTrue(ls.isPublicKeyLoaded());
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/downloadkeys")
				.param("privateKeyName", "test.private")
				.param("publicKeyName", "test.public")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	void downloadKeysWithoutGeneratingOrLoadingInMemory() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/downloadkeys")
				.param("privateKeyName", "test.private")
				.param("publicKeyName", "test.public")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Either or both of the keys are not loaded")));
		
		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		assertFalse(ls.isPrivateKeyLoaded());
		assertFalse(ls.isPublicKeyLoaded());
	}
	
	@Test
	void downloadGeneratedKeysWithEmptyOrInvalidNames() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", "RSA/ECB/PKCS1Padding")
				.param("size", String.valueOf(1024))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Keys have been generated in memory. Download and save them to a secure location if you plan to use them for signing a license")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/downloadkeys")
				.param("privateKeyName", "")
				.param("publicKeyName", "")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.['downloadKeys.publicKeyName']", Matchers.is("Public Key name cannot be blank")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['downloadKeys.privateKeyName']", Matchers.is("Private Key name cannot be blank")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/downloadkeys")
				.param("privateKeyName", "egg...public")
				.param("publicKeyName", "egg.//.private")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isNotAcceptable())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Invalid Key Names")));
	}
}
