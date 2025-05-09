package org.egg.license3j_spring;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LicenseController {
	
	private final LicenseService ls = new LicenseService();
	
	/**
	 * 
	 * @return HTTP:200 if license generation is successful, or else an HTTP:400 (Bad Request) if an unsaved license is in memory
	 */
	@GetMapping("/new")
	public ResponseEntity<String> generateNewLicense() {
		try {
			ls.newLicense();
			return ResponseEntity.ok("A new license has been generated in memory");
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body("License in memory has not been saved. Please save the license first");
		}
	}

}
