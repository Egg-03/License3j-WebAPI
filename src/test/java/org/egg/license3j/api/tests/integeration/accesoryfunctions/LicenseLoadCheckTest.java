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
class LicenseLoadCheckTest {

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
	void freshLicenseLoadCheck() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/isloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(true)));
	}
	
	@Test
	void existingLicenseLoadCheck() throws Exception {
		
		MockMultipartFile mockFile = new MockMultipartFile(
	            "license",
	            "license.bin",
	            MediaType.APPLICATION_OCTET_STREAM.toString(),
	            LicenseLoadCheckTest.class.getResourceAsStream("/license.bin").readAllBytes()
	        );

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/license/upload")
				.file(mockFile)
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/isloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(true)));
	}
	
	@Test
	void badLicenseLoadCheck() throws Exception {
		
		MockMultipartFile mockFile = new MockMultipartFile(
	            "license",
	            "license.bin",
	            MediaType.APPLICATION_OCTET_STREAM.toString(),
	            LicenseLoadCheckTest.class.getResourceAsStream("/InvalidLicense.bin").readAllBytes()
	        );

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/license/upload")
				.file(mockFile)
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/isloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(false)));
	}
	
}

