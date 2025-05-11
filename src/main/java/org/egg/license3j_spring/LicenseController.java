package org.egg.license3j_spring;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax0.license3j.io.IOFormat;

@RestController
@RequestMapping("/api")
public class LicenseController {
	
	private final LicenseService ls = new LicenseService();
	private static final Logger logger = LoggerFactory.getLogger(LicenseController.class);
	
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
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	// example usage: http://localhost:8080/api/license/new
	
	// TODO explore further to see how the file is downloaded
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
	
	@GetMapping("/license/show")
	public ResponseEntity<String> showLicense() {
		try {
			String license = ls.displayLicense();
			return ResponseEntity.ok(license);
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	// example usage: http://localhost:8080/api/license/show
	
	@PostMapping("/license/upload")
	public ResponseEntity<String> uploadLicense(@RequestParam("license") MultipartFile license, @RequestParam IOFormat format) {
		try {		
			ls.loadLicense(license.getInputStream(), format);
			return ResponseEntity.ok().body("License loaded from file");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		} catch (IOException e) {
			logger.error("License input stream error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("License could not be accessed");
		} 
	}
	
	@PostMapping("/license/addfeature")
	public ResponseEntity<String> addFeature(@RequestParam("featureName") String featureName, @RequestParam("featureType") FeatureTypes featureType, @RequestParam("featureContent") String featureContent){
		try {
			ls.addFeature(featureName, featureType, featureContent);
			return ResponseEntity.ok("Feature: "+featureName+" of type "+featureType+" with value "+featureContent+" has been added");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
}
