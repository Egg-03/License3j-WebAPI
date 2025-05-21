package org.egg.license3japi.tests.controllertests;

import org.egg.license3j.api.constants.FeatureType;
import org.egg.license3j.api.controllers.LicenseController;
import org.egg.license3j.api.service.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;

import javax0.license3j.io.IOFormat;

@SpringJUnitWebConfig (classes= {LicenseController.class, LicenseService.class})
class NewLicenseTest {

	@Autowired private WebApplicationContext wac;
	private MockMvc mockMvc;

	@Autowired private MockHttpSession session;
	
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
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
		.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	void generateNewLicenseWhenUnsavedLicenseIsInMemory() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
		.andExpect(MockMvcResultMatchers.status().isOk());
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/addfeature")
				.param("featureName", "MockFeature")
				.param("featureType", FeatureType.STRING.name())
				.param("featureContent", "MockContent")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		// try creating another license in memory via the controller end-point and expect a conflict
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
		.andExpect(MockMvcResultMatchers.status().isConflict())
		.andExpect(MockMvcResultMatchers.content().string("An unsaved license is detected. Please save it first."));
		
	}
	
	@Test
	void generateNewLicenseAfterLoadingAnotherFromAFile() throws Exception {
		
		MockMultipartFile mmpf = new MockMultipartFile("license", "license.bin", "application/octet-stream", NewLicenseTest.class.getResourceAsStream("/license.bin"));
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/license/upload")
				.file(mmpf)
				.param("format", IOFormat.BINARY.name())
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/isloaded").session(session))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
	}

}
