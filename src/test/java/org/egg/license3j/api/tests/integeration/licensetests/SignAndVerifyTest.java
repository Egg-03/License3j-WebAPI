package org.egg.license3j.api.tests.integeration.licensetests;

import org.egg.license3j.api.constants.FeatureType;
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
class SignAndVerifyTest {

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
	void signAndVerifyNewLicenseWithNewKeys() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");	
		ls.generate("RSA/ECB/PKCS1Padding", 1024);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/sign")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License Signed with the keys loaded in memory")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/verify")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License is properly signed.")));
	}
	
	@Test
	void signAndVerifyNewLicenseWithExistingKeys() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");	
		ls.loadPrivateKey(SaveLicenseTest.class.getResourceAsStream("/test.private"), IOFormat.BINARY);
		ls.loadPublicKey(SaveLicenseTest.class.getResourceAsStream("/test.public"), IOFormat.BINARY);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/sign")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License Signed with the keys loaded in memory")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/verify")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License is properly signed.")));
	}
	
	@Test
	void signAndVerifyExistingLicenseWithExistingKeys() throws Exception {
		
		MockMultipartFile mockFile = new MockMultipartFile(
	            "license",
	            "license.bin",
	            MediaType.APPLICATION_OCTET_STREAM.toString(),
	            NewLicenseTest.class.getResourceAsStream("/license.bin").readAllBytes()
	        );

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/license/upload")
				.file(mockFile)
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");	
		ls.loadPrivateKey(SaveLicenseTest.class.getResourceAsStream("/test.private"), IOFormat.BINARY);
		ls.loadPublicKey(SaveLicenseTest.class.getResourceAsStream("/test.public"), IOFormat.BINARY);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/sign")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License Signed with the keys loaded in memory")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/verify")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License is properly signed.")));
	}
	
	@Test
	void signAndVerifyExistingLicenseWithNewKeys() throws Exception {
		
		MockMultipartFile mockFile = new MockMultipartFile(
	            "license",
	            "license.bin",
	            MediaType.APPLICATION_OCTET_STREAM.toString(),
	            NewLicenseTest.class.getResourceAsStream("/license.bin").readAllBytes()
	        );

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/license/upload")
				.file(mockFile)
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");	
		ls.generate("RSA/ECB/PKCS1Padding", 1024);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/sign")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License Signed with the keys loaded in memory")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/verify")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License is properly signed.")));
	}
	
	@Test
	void signAndVerifyWhenNoLicenseInMemory() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/sign")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("No license detected in memory.")));
			
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/verify")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("No license in memory")));
	}
	
	@Test
	void signAndVerifyWhenNoKeyInMemory() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
	
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/sign")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("No private key detected in memory.")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/verify")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("No public key in memory")));
	}
	
	@Test
	void signAndVerifyWhenNoPrivateKeyInMemory() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.loadPublicKey(SaveLicenseTest.class.getResourceAsStream("/test.public"), IOFormat.BINARY);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/sign")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("No private key detected in memory.")));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/verify")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License is NOT properly signed.")));
	}
}

