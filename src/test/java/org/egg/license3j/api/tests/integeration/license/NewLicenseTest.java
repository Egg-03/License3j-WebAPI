package org.egg.license3j.api.tests.integeration.license;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.egg.license3j.api.constants.FeatureType;
import org.egg.license3j.api.service.LicenseService;
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
class NewLicenseTest {

	@Autowired private WebApplicationContext wac;
	private MockMvc mockMvc;

	@Autowired private MockHttpSession session;
	
	@Autowired private LicenseService ls;
	
	@BeforeEach
	void setUp() {
		
		// Create a new session for each test method
        session = new MockHttpSession();
        
		// Set up MockMvc with the actual web application context
        // RequestContextFilter is crucial for @SessionScope to work correctly in tests
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .addFilters(new RequestContextFilter()) // Ensures session scope is properly handled
                .build();
	}
	
	@Test
	void generateNewLicenseInMemory() throws Exception {
			
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
			.session(session))
			.andExpect(MockMvcResultMatchers.status().isOk());
		
		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		assertTrue(ls.isLicenseLoaded());
		assertTrue(ls.licenseRequiresSigning());
		assertFalse(ls.licenseRequiresSaving());
	}
	
	@Test
	void generateNewLicenseWhenUnsavedLicenseIsInMemory() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");

		ls.addFeature("testFeature", FeatureType.STRING, "testContent");

		assertTrue(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isConflict());
		
		assertTrue(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());
	}
	
	@Test
	void uploadNewLicenseWhenUnsavedLicenseIsInMemory() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");

		ls.addFeature("testFeature", FeatureType.STRING, "testContent");

		assertTrue(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());
		
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
				.andExpect(MockMvcResultMatchers.status().isConflict());
		
		assertTrue(ls.licenseRequiresSigning());
		assertTrue(ls.licenseRequiresSaving());
	}
}