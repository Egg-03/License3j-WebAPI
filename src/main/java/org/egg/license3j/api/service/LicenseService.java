package org.egg.license3j.api.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.egg.license3j.api.constants.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax0.license3j.Feature;
import javax0.license3j.License;
import javax0.license3j.crypto.LicenseKeyPair;
import javax0.license3j.io.IOFormat;
import javax0.license3j.io.KeyPairWriter;
import javax0.license3j.io.LicenseReader;
import javax0.license3j.io.LicenseWriter;
import net.lingala.zip4j.ZipFile;


public class LicenseService {
	
	private License license;
	private boolean licenseToSave = false;
	private boolean licenseToSign = false;
	private LicenseKeyPair keyPair;
	
	private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);
	
	// accessory functions
		public Boolean isLicenseLoaded() {
			return license != null;
		}
		
		public Boolean licenseRequiresSaving() {
			return licenseToSave;
		}
		
		public Boolean licenseRequiresSigning() {
			return licenseToSign;
		}
		
		public Boolean isPrivateKeyLoaded() {
			return (keyPair != null && keyPair.getPair() != null && keyPair.getPair().getPrivate() != null);
		}
		
		public Boolean isPublicKeyLoaded() {
			return (keyPair != null && keyPair.getPair() != null && keyPair.getPair().getPublic() != null);
		}
	
	// generate a new license if there are no previously unsaved licenses
		public void newLicense() throws ResponseStatusException {
			if (!licenseToSave) {
				license = new License();
				licenseToSign = true;
				logger.info("A new license has been generated in memory"); 
			} else {
				logger.warn("An unsaved license is detected in memory");
				throw new ResponseStatusException(HttpStatus.CONFLICT, "An unsaved license is detected. Please save it first.");
			}
		}
		
		// save license to file
		public File saveLicense(String licenseName, IOFormat format) throws ResponseStatusException {
			if (license == null) {
				logger.error("No license in memory. Please create or load a license");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No license in memory. Please create or load a license");
			} 
			
			if(licenseToSign) {
				logger.error("License needs to be signed before saving");
				throw new ResponseStatusException(HttpStatus.CONFLICT, "License needs to be signed before saving");
			}
			
			File f = new File(licenseName);
			try (LicenseWriter writer = new LicenseWriter(f)) {
				writer.write(license, format);
				licenseToSave = false;
				logger.info("License Written Successfully");
				return f;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occured during writing the license");
			}
		}
		
		// dump license to screen
		public String displayLicense() throws ResponseStatusException {
			if (license == null) {
				logger.error("No license in memory. Please create or load a license");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No license in memory. Please create or load a license");
			}

			try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); LicenseWriter lw = new LicenseWriter(baos)) {
				lw.write(license, IOFormat.STRING);
				return baos.toString(StandardCharsets.UTF_8);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occured during writing license bytes");
			}
		}
		
		// load an existing license
		public void loadLicense(InputStream licenseInputStream, IOFormat format) throws ResponseStatusException {
			if (licenseToSave) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Unsaved license detected in memory. Please save the license first.");
			}

			try (LicenseReader reader = new LicenseReader(licenseInputStream)) {
				license = reader.read(format);
				licenseToSave = false;
				licenseToSign = false;
				logger.info("License is loaded in memory.");
			} catch (IOException e) {
				logger.error(String.valueOf(e));
				e.printStackTrace();
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "An I/O error occured during loading the license from file");
			} 
		}
		
		// add features to a license
		public void addFeature(String featureName, FeatureType type, String featureContent) throws ResponseStatusException {
			if (license == null) {
				logger.error("No license in memory. Feature cannot be added.");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No license in memory. Please create or load a license");
			}

			license.add(Feature.Create.from(featureName+":"+type+"="+featureContent));
			licenseToSave = true;
			licenseToSign = true;
			logger.info("Feature: {} of type {} with content {} has been added to the license. License must be signed before saving.", featureName, type, featureContent);
		}
		
		// will generate a private-key public-key pair and load it in memory
		private void generateKeys(String algorithm, int size) throws NoSuchAlgorithmException {
				keyPair = LicenseKeyPair.Create.from(algorithm, size);
				logger.info("Private and Public Keys loaded in memory");
		}

		// will save the loaded keys to file
		// uses the generateKeys() method internally

		public void generate(String algorithm, int size) throws ResponseStatusException {
			
			try {
				generateKeys(algorithm, size);
			} catch (NoSuchAlgorithmException e) {
				logger.error("Algorithm Unavailable", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, algorithm+" is not available in the environment");
			}
		}
		
		// return a list of files which contain the key contents previously generated in memory
		
		public File saveKeys(String privateKeyName, String publicKeyName, IOFormat format) {
			
			if(Boolean.TRUE.equals(!isPrivateKeyLoaded()) || Boolean.TRUE.equals(!isPublicKeyLoaded()))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either or both of the keys are not loaded");
			
			File privateKeyFile = new File(privateKeyName);
			File publicKeyFile = new File(publicKeyName);
			
			try (KeyPairWriter writer = new KeyPairWriter(privateKeyFile, publicKeyFile); ZipFile z = new ZipFile("keys.zip")) {
				writer.write(keyPair, format);
				logger.info("Keys have been written for output");		
				z.addFiles(List.of(privateKeyFile, publicKeyFile));
				return z.getFile();
			} catch (IOException e) {
				logger.error("An I/O error occured during writing keys to files", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occured during writing keys to files");
			}
		}
}
