package org.egg.license3j.api.tests.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.egg.license3j.api.constants.FeatureType;
import org.egg.license3j.api.controllers.LicenseController;
import org.egg.license3j.api.service.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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
	
	@MockitoSpyBean private LicenseService ls;
	
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
		
		assertFalse(ls.isLicenseLoaded());
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new")
			.session(session))
			.andExpect(MockMvcResultMatchers.status().isOk());
		
		assertTrue(ls.isLicenseLoaded());
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