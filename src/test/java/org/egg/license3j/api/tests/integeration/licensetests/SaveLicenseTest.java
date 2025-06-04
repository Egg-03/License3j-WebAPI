package org.egg.license3j.api.tests.integeration.licensetests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class SaveLicenseTest {

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
	void saveNewLicenseWithExistingKeys() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");
			
		ls.loadPrivateKey(SaveLicenseTest.class.getResourceAsStream("/test.private"), IOFormat.BINARY);
		ls.loadPublicKey(SaveLicenseTest.class.getResourceAsStream("/test.public"), IOFormat.BINARY);
		ls.signLicense();
		
		assertFalse(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "eggtest.bin")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		assertFalse(ls.licenseRequiresSaving());
	}
	
	@Test
	void saveNewLicenseWithNewKeys() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");
			
		ls.generate("RSA/ECB/PKCS1Padding", 1024);
		ls.signLicense();
		
		assertFalse(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "eggtest.bin")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		assertFalse(ls.licenseRequiresSaving());
	}
	
	@Test
	void saveExistingLicenseWithExistingKeys() throws Exception {

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
		ls.signLicense();
		
		assertFalse(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "eggtest.bin")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		assertFalse(ls.licenseRequiresSaving());
	}
	
	@Test
	void saveExistingLicenseWithNewKeys() throws Exception {

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
		ls.signLicense();
		
		assertFalse(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "eggtest.bin")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		assertFalse(ls.licenseRequiresSaving());
	}
	
	@Test
	void saveLicenseWhenNotLoaded() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "eggtest.bin")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("No license in memory. Please create or load a license")));
	}
	
	@Test
	void saveLicenseWhenNeedsSigning() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
		.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");
		
		assertTrue(ls.licenseRequiresSigning());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "eggtest.bin")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isConflict())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("License needs to be signed before saving")));
	}
	
	@Test
	void saveNewLicenseWithInvalidName() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");
			
		ls.generate("RSA/ECB/PKCS1Padding", 1024);
		ls.signLicense();
		
		assertFalse(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "eggtest..bin")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isNotAcceptable())
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("Invalid File Name")));
		
		assertTrue(ls.licenseRequiresSaving());
	}
	
	@Test
	void saveNewLicenseWithEmptyName() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");
			
		ls.generate("RSA/ECB/PKCS1Padding", 1024);
		ls.signLicense();
		
		assertFalse(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/save")
				.param("licenseName", "")
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.['saveLicense.licenseName']", Matchers.is("License name cannot be blank")));
		
		assertTrue(ls.licenseRequiresSaving());
	}
	
}

