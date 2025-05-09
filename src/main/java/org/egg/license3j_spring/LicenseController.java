package org.egg.license3j_spring;

import java.io.File;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax0.license3j.io.IOFormat;

@RestController
@RequestMapping("/api")
public class LicenseController {
	
	private final LicenseService ls = new LicenseService();
	
	/**
	 * 
	 * @return HTTP:200 if license generation is successful, or else an HTTP:400 (Bad Request) if an unsaved license is in memory
	 */
	@GetMapping("/license/new")
	public ResponseEntity<String> generateNewLicense() {
		try {
			ls.newLicense();
			return ResponseEntity.ok("A new license has been generated in memory");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body("License in memory has not been saved. Please save the license first");
		}
	}
	
	@GetMapping("/license/save")
	public ResponseEntity<File> saveLicense(@RequestParam String licenseName, @RequestParam String format) {
		
		IOFormat ioformat;
		switch(format) {
		case "BINARY", "binary" -> ioformat=IOFormat.BINARY;
		case "TEXT", "text" -> ioformat=IOFormat.STRING;
		case "BASE64", "base64" -> ioformat=IOFormat.BASE64;
		default -> ioformat=IOFormat.BINARY;
		}
		
		try {
			File f = ls.saveLicense(licenseName, ioformat);
			return ResponseEntity.ok(f);
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);	
		}
	}
	// example usage: http://localhost:8080/api/license/save?licenseName=egg.bin&format=BINARY

}
