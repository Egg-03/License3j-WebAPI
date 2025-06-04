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
class DisplayLicenseTest {

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
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(new RequestContextFilter()) // Ensures session
																									// scope is properly
																									// handled
				.build();
	}

	@Test
	void displayNewlyCreatedLicense() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/license/new").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		ls = (LicenseService) session.getAttribute("scopedTarget.licenseService");
		ls.addFeature("TestFeature", FeatureType.STRING, "TestContent");

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/show").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
						MockMvcResultMatchers.jsonPath("$.['License Info']", Matchers.is("TestFeature=TestContent\n")));
	}

	@Test
	void displayUploadedLicense() throws Exception {

		MockMultipartFile mockFile = new MockMultipartFile("license", "license.bin",
				MediaType.APPLICATION_OCTET_STREAM.toString(),
				NewLicenseTest.class.getResourceAsStream("/license.bin").readAllBytes());

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/license/upload").file(mockFile)
				.param("format", IOFormat.BINARY.name()).session(session))
				.andExpect(MockMvcResultMatchers.status().isOk());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/show").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.['License Info']", Matchers.is(
						"licenseSignature:BINARY=SkFancM5W8S9sXcEXHJFIgzRjv+qhayXFOUk0Y03o7tPhN12NAg4OJWat8rybZcduRifXmw+vfxiuBPS68DyoqHepFB1+p5qmZALtrJgW1srFEY00pJl3P57B/fLSC7eub5kn5+z82LTuLQxXAibjrugKqy3weYO0flqPq8iymd5W1xtZ1Aa/4F+6KovZ1TLCCi7Q87oc02JxkkAi9jDHFfl8a9mW7tYDrDmaQL/qAalAzS7OFnEBptrSIGOO1ghYT0FXgGNhD5lsy5ISFTL8xq+gZHaF/YkCAUw+deiLXpTmOprvQ1WhfiHWVTcICI3BDXvASO2WeZG5yJZ2T2NYrUJGhO2iDNg2OO3Fk+hOsjH+5KZ9TsoM/5miqusqLlak1CUW/sVkqzhPGsOn2TxMSAM334/8yIuyqPkjQjUNpLQloVBT56EcV3frU2Co18VMaZheK1Ko0eXyF0zd2it5LmH55qj2tSDVqCdmS+iRoIq8K5ZlieZbtFEXRKZAq/M\nsignatureDigest=SHA-512\nspecialNote=This license was made for testing the License3j API. Never use the license or the keys used to sign the license in public.\n")));
	}
	
	@Test
	void displaLicenseWhenNotInMemory() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/api/license/show").session(session))
				.andExpect(MockMvcResultMatchers.status().isBadRequest()).andExpect(
						MockMvcResultMatchers.jsonPath("$.['License Info']", Matchers.is("No license in memory. Please create or load a license")));
	}

}
