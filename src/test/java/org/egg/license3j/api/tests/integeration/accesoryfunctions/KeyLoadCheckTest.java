package org.egg.license3j.api.tests.integeration.accesoryfunctions;

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
class KeyLoadCheckTest {

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	@Autowired
	private MockHttpSession session;
	
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
	void freshKeyLoadCheck() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/key/generatekeys")
				.param("cipher", "RSA/ECB/PKCS1Padding")
				.param("size", String.valueOf(1024))
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Keys have been generated in memory. Download and save them to a secure location if you plan to use them for signing a license")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/isprivatekeyloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(true)));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/ispublickeyloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(true)));
		
	}
	
	@Test
	void existingKeyLoadCheck() throws Exception {
		
		MockMultipartFile privateKey = new MockMultipartFile("privateKeyFile", "test.private",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyLoadCheckTest.class.getResourceAsStream("/test.private"));
		MockMultipartFile publicKey = new MockMultipartFile("publicKeyFile", "test.public",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyLoadCheckTest.class.getResourceAsStream("/test.public"));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadprivatekey").file(privateKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Private key loaded in memory")));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadpublickey").file(publicKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Public key loaded in memory")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/isprivatekeyloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(true)));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/ispublickeyloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(true)));

	}
	
	@Test
	void invalidKeyLoadCheck() throws Exception {
		
		MockMultipartFile privateKey = new MockMultipartFile("privateKeyFile", "test.private",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyLoadCheckTest.class.getResourceAsStream("/test.public"));
		MockMultipartFile publicKey = new MockMultipartFile("publicKeyFile", "test.public",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, KeyLoadCheckTest.class.getResourceAsStream("/test.private"));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadprivatekey").file(privateKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("The given key specification is invalid")));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/key/uploadpublickey").file(publicKey)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("The given key specification is invalid")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/isprivatekeyloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(false)));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/key/ispublickeyloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(false)));

	}
}

