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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;

import javax0.license3j.io.IOFormat;

@SpringBootTest
@AutoConfigureMockMvc
class KeyUploadTest {

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
	void uploadKeys() throws Exception {

		MockMultipartFile privateKey = new MockMultipartFile("privateKeyFile", "test.private",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyGenerationTest.class.getResourceAsStream("/test.private"));
		MockMultipartFile publicKey = new MockMultipartFile("publicKeyFile", "test.public",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyGenerationTest.class.getResourceAsStream("/test.public"));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadprivatekey").file(privateKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Private key loaded in memory")));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadpublickey").file(publicKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Public key loaded in memory")));

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");

		assertTrue(ls.isPublicKeyLoaded());
		assertTrue(ls.isPrivateKeyLoaded());
	}
	
	@Test
	void uploadInvalidKeys() throws Exception {

		MockMultipartFile privateKey = new MockMultipartFile("privateKeyFile", "test.private",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyGenerationTest.class.getResourceAsStream("/test.public"));
		MockMultipartFile publicKey = new MockMultipartFile("publicKeyFile", "test.public",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyGenerationTest.class.getResourceAsStream("/test.private"));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadprivatekey").file(privateKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("The given key specification is invalid")));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadpublickey").file(publicKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("The given key specification is invalid")));

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");

		assertFalse(ls.isPublicKeyLoaded());
		assertFalse(ls.isPrivateKeyLoaded());
	}
}