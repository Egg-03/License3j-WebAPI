package org.egg.license3j.api.tests.integeration.keymanagementtests;

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

@SpringBootTest
@AutoConfigureMockMvc
class KeyGenerationTest {

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
	void generateNewKeys() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", "RSA/ECB/PKCS1Padding")
				.param("size", String.valueOf(1024))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Keys have been generated in memory. Download and save them to a secure location if you plan to use them for signing a license")));
		
		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		assertTrue(ls.isPrivateKeyLoaded());
		assertTrue(ls.isPublicKeyLoaded());
	}
	
	@Test
	void generateNewKeysWithUnsupportedCiphers() throws Exception {
		
		String cipherName = "TestCipher";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", cipherName)
				.param("size", String.valueOf(1024))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(cipherName+" is not available in the environment")));
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", "")
				.param("size", String.valueOf(1024))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.['generateKeys.cipher']", Matchers.is("Cipher specification cannot be blank")));
		
		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		assertFalse(ls.isPrivateKeyLoaded());
		assertFalse(ls.isPublicKeyLoaded());
	}
	
	@Test
	void generateNewKeysWithUnsupportedCipherSizes() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", "RSA/ECB/PKCS1Padding")
				.param("size", String.valueOf(512))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.['generateKeys.size']", Matchers.is("Size must be at least 1024")));
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", "RSA/ECB/PKCS1Padding")
				.param("size", String.valueOf(4096))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.['generateKeys.size']", Matchers.is("Size cannot exceed 3072")));
		
	}
}


