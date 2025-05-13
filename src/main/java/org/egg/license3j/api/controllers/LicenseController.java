package org.egg.license3j.api.controllers;

import java.io.IOException;

import org.egg.license3j.api.constants.FeatureType;
import org.egg.license3j.api.service.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
	
	private final LicenseService ls;
	private static final Logger logger = LoggerFactory.getLogger(LicenseController.class);
	
	@Autowired
	public LicenseController(LicenseService ls) {
		this.ls=ls;
	}
	
	/**
	 * 
	 * @return HTTP:200 if license generation is successful, or else an HTTP:400 (Bad Request) if an unsaved license is in memory
	 */
	@PostMapping("/license/new")
	public ResponseEntity<String> generateNewLicense() {
		try {
			ls.newLicense();
			return ResponseEntity.ok("A new license has been generated in memory");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	// example usage: http://localhost:8080/api/license/new
	
	@GetMapping("/license/save")
	public ResponseEntity<Resource> saveLicense(@RequestParam String licenseName, @RequestParam IOFormat format) {
			
		try {
			Resource licenseFile = ls.saveLicense(licenseName, format);
			HttpHeaders headers = new HttpHeaders();
	        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+licenseName);
			return ResponseEntity.ok()
					.headers(headers)
					.contentLength(licenseFile.contentLength())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(licenseFile);
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);	
		} catch (IOException e) {
			logger.error("An I/O Exception occured during getting content length for the license file", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
	public ResponseEntity<String> addFeature(@RequestParam("featureName") String featureName, @RequestParam("featureType") FeatureType featureType, @RequestParam("featureContent") String featureContent){
		try {
			ls.addFeature(featureName, featureType, featureContent);
			return ResponseEntity.ok("Feature: "+featureName+" of type "+featureType+" with value "+featureContent+" has been added");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	@PostMapping("/key/generatekeys")
	public ResponseEntity<String> generateKeys(@RequestParam("cipher") String cipher, @RequestParam("size") int size){
		try {
			ls.generate(cipher, size);
			return ResponseEntity.ok("Keys have been generated in memory. Download and save them to a secure location if you plan to use them for signing a license");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	@GetMapping("/key/downloadkeys")
	public ResponseEntity<Resource> downloadKeys(@RequestParam("privateKeyName") String privateKeyName, @RequestParam("publicKeyName") String publicKeyName, @RequestParam("format") IOFormat format){
		try {
			Resource zippedKeys = ls.saveKeys(privateKeyName, publicKeyName, format);
			
			HttpHeaders headers = new HttpHeaders();
	        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=keys.zip");
			return ResponseEntity.ok()
					.headers(headers)
					.contentLength(zippedKeys.contentLength())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(zippedKeys);
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);
		} catch (IOException e) {
			logger.error("An I/O Exception occured during getting content length for the zipped key files", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@PostMapping("/key/uploadprivatekey")
	public ResponseEntity<String> uploadPrivateKey(@RequestParam("privateKeyFile") MultipartFile privateKeyFile, @RequestParam IOFormat format) {
		try {
			ls.loadPrivateKey(privateKeyFile.getInputStream(), format);
			return ResponseEntity.ok("Private key loaded in  memory");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		} catch (IOException e) {
			logger.error("Private key input stream error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Public key could not be accessed");
		}
	}
	
	@PostMapping("/key/uploadpublickey")
	public ResponseEntity<String> uploadPublicKey(@RequestParam("publicKeyFile") MultipartFile publicKeyFile, @RequestParam IOFormat format) {
		try {
			ls.loadPublicKey(publicKeyFile.getInputStream(), format);
			return ResponseEntity.ok("Public key loaded in  memory");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		} catch (IOException e) {
			logger.error("Public key input stream error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Public key could not be accessed");
		}
	}
	
	@GetMapping("/key/digestpublickey")
	public ResponseEntity<String> digestPublicKey() {
		try {
			return ResponseEntity.ok(ls.digestPublicKey());
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	@PostMapping("/license/sign")
	public ResponseEntity<String> signLicense() {
		try {
			ls.signLicense();
			return ResponseEntity.ok("License Signed with the keys loaded in memory");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	@GetMapping("/license/verify")
	public ResponseEntity<String> verifyLicense() {
		try {
			String licenseSignStatus = ls.verifyLicense();
			return ResponseEntity.ok(licenseSignStatus);
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().getDetail());
		}
	}
	
	//accessory functions
	
	@GetMapping("/license/isloaded")
	public ResponseEntity<Boolean> isLicenseLoaded() {
		return ResponseEntity.ok(ls.isLicenseLoaded());
	}
	
	@GetMapping("/license/requiressigning")
	public ResponseEntity<Boolean> licenseRequiresSigning() {
		return ResponseEntity.ok(ls.licenseRequiresSigning());
	}
	
	@GetMapping("/license/requiressaving")
	public ResponseEntity<Boolean> licenseRequiresSaving() {
		return ResponseEntity.ok(ls.licenseRequiresSaving());
	}
	
	@GetMapping("/key/isprivatekeyloaded")
	public ResponseEntity<Boolean> isPrivateKeyLoaded() {
		return ResponseEntity.ok(ls.isPrivateKeyLoaded());
	}
	
	@GetMapping("/key/ispublickeyloaded")
	public ResponseEntity<Boolean> isPublicKeyLoaded() {
		return ResponseEntity.ok(ls.isPublicKeyLoaded());
	}
	
	@GetMapping("/healthcheck")
	public ResponseEntity<String> healthcheck() {
		return ResponseEntity.ok("Active");
	}
}
