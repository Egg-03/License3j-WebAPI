package org.egg.license3j.api.tests.integration;

import org.egg.license3j.api.constants.FeatureType;
import org.egg.license3j.api.controllers.LicenseController;
import org.egg.license3j.api.service.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;

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
		
		// expect the licenseToSign signal to return true and licenseToSave signal to return false
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressigning")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(false)));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressaving")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(false)));
		
		// generate a fresh license in memory and expect an OK status
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").
				session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		// check whether the license was loaded
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/isloaded")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
		
		// expect the licenseToSign signal to return true and licenseToSave signal to return false
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressigning")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressaving")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(false)));
		
		// generate another fresh license in memory and expect an OK status
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
		.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	void generateNewLicenseWhenUnsavedLicenseIsInMemory() throws Exception {
		
		// try creating a fresh license in memory via the controller end-point and expect an OK status
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		// try adding features to the fresh license via the controller end-point and expect an OK status
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/addfeature")
				.param("featureName", "MockFeature")
				.param("featureType", FeatureType.STRING.name())
				.param("featureContent", "MockContent")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		// expect the licenseToSign and licenseToSave signals to return true
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressigning")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressaving")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
		
		
		// try creating another license in memory over the unsaved license, via the controller end-point and expect a conflict
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isConflict())
				.andExpect(MockMvcResultMatchers.content().string("An unsaved license is detected. Please save it first."));
		
		
		// make sure that conflict does not change the state of the license signals
		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressigning")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/requiressaving")
				.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
	}
}