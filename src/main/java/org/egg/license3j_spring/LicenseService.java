package org.egg.license3j_spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax0.license3j.License;
import javax0.license3j.crypto.LicenseKeyPair;

public class LicenseService {
	
	private License license;
	private boolean licenseToSave = false;
	private boolean licenseToSign = false;
	private LicenseKeyPair keyPair;
	private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

	// generate a new license if there are no previously unsaved licenses
		public void newLicense() {
			if (!licenseToSave) {
				license = new License();
				licenseToSign = true;
				logger.info("A new license has been generated in memory"); 
			} else {
				logger.warn("An unsaved license is detected in memory");
				throw new IllegalStateException("An unsaved license is detected in memory. Please save the license first.");
			}
		}

}
