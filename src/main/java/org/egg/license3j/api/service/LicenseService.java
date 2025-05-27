package org.egg.license3j.api.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.egg.license3j.api.constants.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.server.ResponseStatusException;

import javax0.license3j.Feature;
import javax0.license3j.License;
import javax0.license3j.crypto.LicenseKeyPair;
import javax0.license3j.io.IOFormat;
import javax0.license3j.io.KeyPairReader;
import javax0.license3j.io.KeyPairWriter;
import javax0.license3j.io.LicenseReader;
import javax0.license3j.io.LicenseWriter;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

@Component
@SessionScope
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
		
		public Boolean isFileNameValid(String fileName) {
			return !fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\");
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
		public ByteArrayResource saveLicense(String licenseName, IOFormat format) throws ResponseStatusException {
			
			if(Boolean.FALSE.equals(isFileNameValid(licenseName))) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid File Name");
			}
			
			if (license == null) {
				logger.error("No license in memory. Please create or load a license");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No license in memory. Please create or load a license");
			} 
			
			if(licenseToSign) {
				logger.error("License needs to be signed before saving");
				throw new ResponseStatusException(HttpStatus.CONFLICT, "License needs to be signed before saving");
			}
			
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); LicenseWriter writer = new LicenseWriter(baos)) {
				writer.write(license, format);
				licenseToSave = false;
				logger.info("License Written Successfully");
				return new ByteArrayResource(baos.toByteArray());
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
				logger.error("An I/O error occured during loading the license from file", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occured during loading the license from file");
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

		public void generate(String cipher, int size) throws ResponseStatusException {
			
			try {
				generateKeys(cipher, size);
			} catch (NoSuchAlgorithmException e) {
				logger.error("Algorithm Unavailable", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, cipher+" is not available in the environment");
			}
		}
		
		// bundle the keys in a zip format for download
		public ByteArrayResource saveKeys(String privateKeyName, String publicKeyName, IOFormat format) throws ResponseStatusException {
			
			if(Boolean.FALSE.equals(isFileNameValid(privateKeyName)) || Boolean.FALSE.equals(isFileNameValid(publicKeyName))) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid Key Names");
			}
			
			if(Boolean.TRUE.equals(!isPrivateKeyLoaded()) || Boolean.TRUE.equals(!isPublicKeyLoaded()))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either or both of the keys are not loaded");			
			
			try (ByteArrayOutputStream privateKeyOutputStream = new ByteArrayOutputStream();
					ByteArrayOutputStream publicKeyOutputStream = new ByteArrayOutputStream();
					ByteArrayOutputStream zippedKeysOutputStream = new ByteArrayOutputStream();
					KeyPairWriter writer = new KeyPairWriter(privateKeyOutputStream, publicKeyOutputStream)) {
				
				writer.write(keyPair, format);
				logger.info("Keys have been written for output");
				
				try (ZipOutputStream zos = new ZipOutputStream(zippedKeysOutputStream)) {

					// Write private key entry
					ZipParameters privateParams = new ZipParameters();
					privateParams.setCompressionMethod(CompressionMethod.DEFLATE);
					privateParams.setCompressionLevel(CompressionLevel.NORMAL);
					privateParams.setFileNameInZip(privateKeyName);
					privateParams.setEntrySize(privateKeyOutputStream.size());
					privateParams.setLastModifiedFileTime(System.currentTimeMillis());
					zos.putNextEntry(privateParams);
					zos.write(privateKeyOutputStream.toByteArray());
					zos.closeEntry();

					// Write public key entry
					ZipParameters publicParams = new ZipParameters();
					publicParams.setCompressionMethod(CompressionMethod.DEFLATE);
					publicParams.setCompressionLevel(CompressionLevel.NORMAL);
					publicParams.setFileNameInZip(publicKeyName);
					publicParams.setEntrySize(publicKeyOutputStream.size());
					publicParams.setLastModifiedFileTime(System.currentTimeMillis());
					zos.putNextEntry(publicParams);
					zos.write(publicKeyOutputStream.toByteArray());
					zos.closeEntry();
				}

	                        
				return new ByteArrayResource(zippedKeysOutputStream.toByteArray());
			} catch (IOException e) {
				logger.error("An I/O error occured during writing keys to files", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occured during writing keys to files");
			}
		}
		
		private LicenseKeyPair merge(LicenseKeyPair oldKp, LicenseKeyPair newKp) {
			if (oldKp == null) {
				return newKp;
			}
			final String cipher = oldKp.cipher();
			if (newKp.getPair().getPublic() != null) {
				return LicenseKeyPair.Create.from(newKp.getPair().getPublic(), oldKp.getPair().getPrivate(), cipher);
			}
			if (newKp.getPair().getPrivate() != null) {
				return LicenseKeyPair.Create.from(oldKp.getPair().getPublic(), newKp.getPair().getPrivate(), cipher);
			}
			return oldKp;
		}

		// load private key
		public void loadPrivateKey(InputStream keyFile, IOFormat format) throws ResponseStatusException {
			if (Boolean.TRUE.equals(isPrivateKeyLoaded())) {
				logger.info("Private Key in memory will be overriden by a new key loaded from a file.");
			}

			try (KeyPairReader kpread = new KeyPairReader(keyFile)) {
				keyPair = merge(keyPair, kpread.readPrivate(format));
				logger.info("Private Key Loaded");
			} catch (IOException  e) {
				logger.error("An I/O error occured while loading private key", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occured while loading private key");
			} catch (InvalidKeySpecException e) {
				logger.error("An error occured while loading private key", e);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given key specification is invalid");
			} catch (NoSuchAlgorithmException e) {
				logger.error("An error occured while loading private key", e);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The algorithm with which the key was created seems to be unvailable in the current environment.");
			}
		}

		// load public key
		public void loadPublicKey(InputStream keyFile, IOFormat format) throws ResponseStatusException  {
			if (Boolean.TRUE.equals(isPublicKeyLoaded())) {
				logger.info("Public Key in memory will be overriden by a new key loaded from a file.");
			}


			try (KeyPairReader kpread = new KeyPairReader(keyFile)) {
				keyPair = merge(keyPair, kpread.readPublic(format));
				logger.info("Public Key Loaded");
			} catch (IOException  e) {
				logger.error("An I/O error occured while loading public key", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occured during writing keys to files");
			} catch (InvalidKeySpecException e) {
				logger.error("An error occured while loading public key", e);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given key specification is invalid");
			} catch (NoSuchAlgorithmException e) {
				logger.error("An error occured while loading public key", e);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The algorithm with which the key was created seems to be unvailable in the current environment.");
			}
		}
		
		// digest public key
		public String digestPublicKey() throws ResponseStatusException {

			if (keyPair == null) {
				logger.error("No digestable public key loaded.");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No public key in memory that can be digested.");
			}

			byte[] publicKey = keyPair.getPublic();
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("SHA-512");
				byte[] calculatedDigest = digest.digest(publicKey);

				StringBuilder javaCode = new StringBuilder("--KEY DIGEST START\nbyte [] digest = new byte[] {\n");
				for (int i = 0; i < calculatedDigest.length; i++) {
					int intVal = (calculatedDigest[i]) & 0xff;
					javaCode.append(String.format("(byte)0x%02X, ", intVal));
					if (i % 8 == 0) {
						javaCode.append("\n");
					}
				}
				javaCode.append("\n};\n---KEY DIGEST END\n");

				javaCode.append("--KEY START\nbyte [] key = new byte[] {\n");
				for (int i = 0; i < publicKey.length; i++) {
					int intVal = (publicKey[i]) & 0xff;
					javaCode.append(String.format("(byte)0x%02X, ", intVal));
					if (i % 8 == 0) {
						javaCode.append("\n");
					}
				}
				
				javaCode.append("\n};\n---KEY END\n");
				return javaCode.toString();
				
			} catch (NoSuchAlgorithmException e) {
				logger.error("Message Digest Algorithm could not be loaded", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Message Digest Algorithm could not be loaded.");
			}

		}
		
		// sign license
		public void signLicense() throws ResponseStatusException {
			if (license == null) {
				logger.error("No license detected in memory. Load or create a license.");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No license detected in memory.");
				
			} else if (Boolean.FALSE.equals(isPrivateKeyLoaded())) {
				logger.error("Private Key not loaded in memory");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No private key detected in memory.");
				
			} else {
				try {
					license.sign(keyPair.getPair().getPrivate(), "SHA-512");
					logger.info("License Signed. Please save before closing the app");
					licenseToSave = true;
		            licenseToSign = false;
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException
						| IllegalBlockSizeException e) {
					logger.error("Signing failed", e);
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed To Sign License ");
				}
				
			}
		}
		
		// verify license
		public String verifyLicense() {
			if (license == null) {
				logger.error("No license loaded in memory to be verified.");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No license in memory");
			}
			if (Boolean.FALSE.equals(isPublicKeyLoaded())) {
				logger.error("No public key loaded in memory to be verified with.");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No public key in memory");
			}
			
			if (license.isOK(keyPair.getPair().getPublic())) {
				return "License is properly signed.";
			} else {
				return "License is NOT properly signed.";
			}
		}
}
