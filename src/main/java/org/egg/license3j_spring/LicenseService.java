package org.egg.license3j_spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax0.license3j.License;
import javax0.license3j.crypto.LicenseKeyPair;
import javax0.license3j.io.IOFormat;
import javax0.license3j.io.LicenseWriter;

public class LicenseService {
	
	private License license;
	private boolean licenseToSave = false;
	private boolean licenseToSign = false;
	private LicenseKeyPair keyPair;
	private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

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
				throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "No license in memory. Please create or load a license");
			} 
			
			if(licenseToSign) {
				logger.error("License needs to be signed before saving");
				throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "License needs to be signed before saving");
			}
			
			File f = new File(licenseName);
			try (LicenseWriter writer = new LicenseWriter(f)) {
				writer.write(license, format);
				licenseToSave = false;
				logger.info("");
				return f;
			} catch (IOException e) {
				logger.error(String.valueOf(e));
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occured during writing the license");
			}
		}

}
