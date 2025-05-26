package org.egg.license3j.api.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import javax0.license3j.io.IOFormat;

@RestController
@Validated
@RequestMapping("/api")
public class LicenseController {
	
	private final LicenseService ls;
	private static final Logger logger = LoggerFactory.getLogger(LicenseController.class);
	
	@Autowired
	public LicenseController(LicenseService ls) {
		this.ls=ls;
	}
	
	@PostMapping(value ="/license/new", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> generateNewLicense() {
		try {
			ls.newLicense();
			return ResponseEntity.ok(Collections.singletonMap("status", "A new license has been generated in memory"));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		}
	}
	
	@GetMapping("/license/save")
	public ResponseEntity<Resource> saveLicense(
			@RequestParam @NotBlank(message = "License name cannot be blank") String licenseName,
			@RequestParam IOFormat format) {
			
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
	
	@GetMapping(value = "/license/show", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> showLicense() {
		try {
			String license = ls.displayLicense();
			return ResponseEntity.ok(Collections.singletonMap("License Info", license));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("License Info", e.getBody().getDetail()));
		}
	}
	
	
	@PostMapping(value = "/license/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> uploadLicense(
			@RequestParam("license") @NotNull(message = "License file cannot be null") MultipartFile license, 
			@RequestParam("format") IOFormat format) {
		try {		
			ls.loadLicense(license.getInputStream(), format);
			return ResponseEntity.ok().body(Collections.singletonMap("status", "License loaded from file"));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		} catch (IOException e) {
			logger.error("License input stream error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("status", "License could not be read"));
		} 
	}
	
	@PostMapping(value = "/license/addfeature", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> addFeature(
			@RequestParam("featureName") @NotBlank(message = "Feature name cannot be blank") String featureName, 
			@RequestParam("featureType") FeatureType featureType, 
			@RequestParam("featureContent") @NotBlank(message = "Feature content cannot be blank") String featureContent){
		
		try {
			ls.addFeature(featureName, featureType, featureContent);
			return ResponseEntity.ok(Collections.singletonMap("status", "Feature: "+StringEscapeUtils.escapeHtml4(featureName)+" of type "+StringEscapeUtils.escapeHtml4(featureType.toString())+" with value "+StringEscapeUtils.escapeHtml4(featureContent)+" has been added"));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		}
	}
	
	@PostMapping(value = "/key/generatekeys", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> generateKeys(
			@RequestParam("cipher") @NotBlank(message = "Cipher specification cannot be blank") String cipher, 
			@RequestParam("size") @NotNull @Min(value = 1024, message = "Size must be at least 1024") @Max(value = 3072, message = "Size cannot exceed 3072") int size) {
		try {
			ls.generate(cipher, size);
			return ResponseEntity.ok(Collections.singletonMap("status", "Keys have been generated in memory. Download and save them to a secure location if you plan to use them for signing a license"));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		}
	}
	
	@GetMapping("/key/downloadkeys")
	public ResponseEntity<Resource> downloadKeys(
			@RequestParam("privateKeyName") @NotBlank(message = "Private Key name cannot be blank") String privateKeyName, 
			@RequestParam("publicKeyName") @NotBlank(message = "Public Key name cannot be blank") String publicKeyName, 
			@RequestParam("format") IOFormat format){
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
	
	@PostMapping(value ="/key/uploadprivatekey", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> uploadPrivateKey(
			@RequestParam("privateKeyFile") @NotNull(message = "Private Key file cannot be null") MultipartFile privateKeyFile, 
			@RequestParam IOFormat format) {
		try {
			ls.loadPrivateKey(privateKeyFile.getInputStream(), format);
			return ResponseEntity.ok(Collections.singletonMap("status", "Private key loaded in memory"));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		} catch (IOException e) {
			logger.error("Private key input stream error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("status", "Private Key could not be read"));
		}
	}
	
	@PostMapping(value = "/key/uploadpublickey", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> uploadPublicKey(
			@RequestParam("publicKeyFile") @NotNull(message = "Public Key file cannot be null") MultipartFile publicKeyFile, 
			@RequestParam IOFormat format) {
		try {
			ls.loadPublicKey(publicKeyFile.getInputStream(), format);
			return ResponseEntity.ok(Collections.singletonMap("status", "Public key loaded in memory"));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		} catch (IOException e) {
			logger.error("Public key input stream error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("status", "Public Key could not be read"));
		}
	}
	
	@GetMapping(value = "/key/dumppublickey", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> digestPublicKey() {
		try {
			return ResponseEntity.ok(Collections.singletonMap("publickey", ls.digestPublicKey()));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("publickey", e.getBody().getDetail()));
		}
	}
	
	@PostMapping(value = "/license/sign", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> signLicense() {
		try {
			ls.signLicense();
			return ResponseEntity.ok(Collections.singletonMap("status", "License Signed with the keys loaded in memory"));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		}
	}
	
	@GetMapping(value = "/license/verify", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> verifyLicense() {
		try {
			String licenseSignStatus = ls.verifyLicense();
			return ResponseEntity.ok(Collections.singletonMap("status", licenseSignStatus));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("status", e.getBody().getDetail()));
		}
	}
	
	//accessory functions
	
	@GetMapping(value = "/license/isloaded", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Boolean>> isLicenseLoaded() {
		return ResponseEntity.ok(Collections.singletonMap("status", ls.isLicenseLoaded()));
	}
	
	@GetMapping(value = "/license/requiressigning", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Boolean>> licenseRequiresSigning() {
		return ResponseEntity.ok(Collections.singletonMap("status", ls.licenseRequiresSigning()));
	}
	
	@GetMapping(value = "/license/requiressaving", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Boolean>> licenseRequiresSaving() {
		return ResponseEntity.ok(Collections.singletonMap("status", ls.licenseRequiresSaving()));
	}
	
	@GetMapping(value = "/key/isprivatekeyloaded", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Boolean>> isPrivateKeyLoaded() {
		return ResponseEntity.ok(Collections.singletonMap("status", ls.isPrivateKeyLoaded()));
	}
	
	@GetMapping(value = "/key/ispublickeyloaded", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Boolean>> isPublicKeyLoaded() {
		return ResponseEntity.ok(Collections.singletonMap("status", ls.isPublicKeyLoaded()));
	}
	
	@GetMapping(value = "/healthcheck", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> healthcheck() {
		return ResponseEntity.ok(Collections.singletonMap("status", "active"));
	}
	
	@GetMapping(value = "/sessionid", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getSessionId(HttpSession session) {
		if(session==null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("SessionID", "Not Found"));
		}
		return ResponseEntity.ok(Collections.singletonMap("SessionID", session.getId()));
	}
}
