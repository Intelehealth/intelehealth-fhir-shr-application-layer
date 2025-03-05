package org.ih.shr.service.impl;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.ih.shr.config.FhirConfig;
import org.ih.shr.service.BundleService;
import org.ih.shr.service.PatientMpiService;
import org.ih.shr.utils.HttpWebClient;
import org.ih.shr.utils.IHConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;

@Service
public class PatientMpiServiceImpl extends IHConstant implements PatientMpiService {

	@Autowired
	BundleService bundleService;

	@Autowired
	private FhirConfig firFhirConfig;

	FhirContext fhirContext = FhirContext.forR4();

	@Override
	public String sendPatient(String bundleString) throws UnsupportedEncodingException {
		Bundle bundle = bundleService.convertToBundle(bundleString);

		if (!hasMPI(bundle)) {
			for (BundleEntryComponent bundleEntry : bundle.getEntry()) {

				Resource resource = (Resource) bundleEntry.getResource();
				Patient patient = (Patient) bundleEntry.getResource();
				// resource.setId("");

				Bundle searchBundle = searchBundle(patient);
				String searchBundleFirstItemResourceId = extractResourceId(searchBundle);
				System.err.println("Search Bundle Resource ID: " + searchBundleFirstItemResourceId);

				String mpiId = null;

				if (!hasMPI(searchBundle) && searchBundleFirstItemResourceId != null) {
					mpiId = searchBundleFirstItemResourceId;
				} else if (hasMPI(searchBundle)) {
					mpiId = getMPI(searchBundle);
				}

				if (mpiId == null) {

					Bundle transactionBundleOne = new Bundle();
					transactionBundleOne.setType(Bundle.BundleType.TRANSACTION);

					Bundle.BundleEntryComponent component = transactionBundleOne.addEntry();
					component.getRequest().setUrl(null).setMethod(Bundle.HTTPVerb.POST);
					component.setResource(resource);

					Bundle bundle1stResponse = firFhirConfig.getOpenCRFhirContext().transaction()
							.withBundle(transactionBundleOne).execute();

					String payload = fhirContext.newJsonParser().setPrettyPrint(true)
							.encodeResourceToString(bundle1stResponse);

					// End of first transaction in openCR/central @FHIR server to get the resourceId

					System.err.println("DDD>>>>>> : payload >>> " + payload);

					mpiId = extractResponseId(bundle1stResponse);

					System.err.println("DDD>>>>>> : mpiId: " + mpiId);

					Bundle transactionBundleTwo = new Bundle();
					transactionBundleTwo.setType(Bundle.BundleType.TRANSACTION);
					patient.setId(mpiId);
					patient.getIdentifier().add(getNewMPIIdentifierWithMpiID(bundle, mpiId));

					Bundle.BundleEntryComponent componentTwo = transactionBundleTwo.addEntry();
					componentTwo.getRequest().setUrl(resource.fhirType() + "/" + mpiId).setMethod(Bundle.HTTPVerb.PUT);
					componentTwo.setResource(patient);

					payload = fhirContext.newJsonParser().setPrettyPrint(true)
							.encodeResourceToString(transactionBundleTwo);

					Bundle bundle2ndResponse = firFhirConfig.getOpenCRFhirContext().transaction()
							.withBundle(transactionBundleTwo).execute();

					payload = fhirContext.newJsonParser().setPrettyPrint(true)
							.encodeResourceToString(transactionBundleTwo);

					System.err.println("DDD>>>>>> : payload" + payload);

					return payload;

				} else {
					Bundle transactionBundleTwo = new Bundle();
					transactionBundleTwo.setType(Bundle.BundleType.TRANSACTION);
					patient.setId(mpiId);

					if (!hasMPI(searchBundle)) {
						patient = mergeIdentifier(patient, searchBundle);
						patient.getIdentifier().add(getNewMPIIdentifierWithMpiID(bundle, mpiId));
					} else {
						patient = mergeIdentifier(patient, searchBundle);
					}

					Bundle.BundleEntryComponent componentTwo = transactionBundleTwo.addEntry();
					componentTwo.getRequest().setUrl(resource.fhirType() + "/" + mpiId).setMethod(Bundle.HTTPVerb.PUT);
					componentTwo.setResource(patient);

					String payload = fhirContext.newJsonParser().setPrettyPrint(true)
							.encodeResourceToString(transactionBundleTwo);

					Bundle bundle2ndResponse = firFhirConfig.getOpenCRFhirContext().transaction()
							.withBundle(transactionBundleTwo).execute();

					payload = fhirContext.newJsonParser().setPrettyPrint(true)
							.encodeResourceToString(transactionBundleTwo);

					System.err.println("DDD>>>>>> : payload" + payload);

					return payload;
				}
			}

		} else {
			Bundle transactionBundle = new Bundle();
			transactionBundle.setType(Bundle.BundleType.TRANSACTION);

			for (BundleEntryComponent bundleEntry : bundle.getEntry()) {

				Resource resource = (Resource) bundleEntry.getResource();

				String mpiId = getMPI(bundle);
				resource.setId(mpiId);
				Bundle.BundleEntryComponent component = transactionBundle.addEntry();
				component.getRequest().setUrl(resource.fhirType() + "/" + mpiId).setMethod(Bundle.HTTPVerb.PUT);
				component.setResource(resource);

				firFhirConfig.getOpenCRFhirContext().transaction().withBundle(transactionBundle).execute();

				String payload = fhirContext.newJsonParser().setPrettyPrint(true)
						.encodeResourceToString(transactionBundle);

				return payload;
			}

		}
		return null;
	}

	private Identifier copyMPIIdentifier(Bundle searchBundle) {
		for (BundleEntryComponent bundleEntry : searchBundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			for (Identifier identifier : patient.getIdentifier()) {
				if (identifier.getType().getText().equals("MPI")) {
					return identifier;
				}
			}
		}
		return null;
	}

	public boolean hasMPI(Bundle bundle) {
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			for (Identifier identifier : patient.getIdentifier()) {
				if (identifier.getType().getText().equals("MPI")) {
					return true;
				}
			}
		}
		return false;
	}

	public String getMPI(Bundle bundle) {
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			for (Identifier identifier : patient.getIdentifier()) {
				if (identifier.getType().getText().equals("MPI")) {
					return identifier.getValue();
				}
			}
		}
		return null;
	}

	public Identifier getNewMPIIdentifierWithMpiID(Bundle bundle, String mpiId) {
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			if (bundleEntry.getResource() instanceof Patient) { // Ensure the resource is a Patient
				Patient patient = (Patient) bundleEntry.getResource();
				for (Identifier identifier : patient.getIdentifier()) {
					Identifier mpiIdentifier = identifier.copy();
					mpiIdentifier.setId(UUID.randomUUID().toString());
					mpiIdentifier.setValue(mpiId);

					// Ensure the coding list is initialized and has at least one element
					if (mpiIdentifier.getType().getCoding().isEmpty()) {
						mpiIdentifier.getType().addCoding(new Coding());
					}

					mpiIdentifier.getType().setText("MPI");
					mpiIdentifier.setSystem(CENTRAL_FHIR+"/StructureDefinition/MPI");
					mpiIdentifier.getType().getCoding().get(0).setSystem(CENTRAL_FHIR+"/CodeSystem/MPI");
					mpiIdentifier.getType().getCoding().get(0).setCode("MPI");
					return mpiIdentifier;
				}
			}
		}
		return null;
	}

	private List<Identifier> getIdentifier(Bundle bundle) {
		if (bundle.getEntry().size() == 0)
			return new ArrayList<>();

		Patient patient = (Patient) bundle.getEntryFirstRep().getResource();

		return patient.getIdentifier();
	}

	private String extractResourceId(Bundle bundle) {
		if (bundle.getEntry().size() == 0)
			return null;
		Resource resource = bundle.getEntryFirstRep().getResource();
		return resource.getIdElement().getIdPart();
	}

	private String extractResponseId(Bundle bundle) {
		if (bundle.getEntry().size() == 0)
			return null;
		BundleEntryResponseComponent response = bundle.getEntryFirstRep().getResponse();
		return response.getLocation().split("/")[1];
	}

	private String makeQueryParam(Patient patient) {

		if (patient == null)
			return "";

		StringBuilder sb = new StringBuilder();

		if (patient.getBirthDate() != null) {
			String dob = new SimpleDateFormat("yyyy-MM-dd").format(patient.getBirthDate()).toString();
			sb.append("&birthdate=").append(dob);
		}

		if (patient.getGender() != null) {
			sb.append("&gender=").append(patient.getGender().toString().toLowerCase());
		}

		if (patient.getName() != null && !patient.getName().isEmpty()) {
			sb.append("&family=").append(patient.getName().get(0).getFamily());
		}

		if (patient.getName() != null && !patient.getName().isEmpty()) {
			sb.append("&given=").append(patient.getName().get(0).getGivenAsSingleString());
		}

		if (sb.length() > 0)
			return sb.substring(1);

		return sb.toString();

	}

	private Bundle searchBundle(Patient patient) throws UnsupportedEncodingException {
		String queryParam = makeQueryParam(patient);

		String response = HttpWebClient.get(opencrOpenhimURL + "/Patient", "?" + queryParam,
				getOpencrOpenhimCredentials()[0], getOpencrOpenhimCredentials()[1]);

		System.err.println("Search bundle response: >>>>> " + response);

		return fhirContext.newJsonParser().parseResource(Bundle.class, response);
	}

	private List<Identifier> getIdentifiers(Bundle bundle) {
		if (bundle.getEntry().size() < 1) {
			return new ArrayList<>();
		}

		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			return patient.getIdentifier();

		}
		return new ArrayList<>();
	}

	private Patient mergeIdentifier(Patient patient, Bundle bundle) {
		List<Identifier> identifiers = getIdentifiers(bundle);
		for (Identifier identifier : identifiers) {
			if (matchWithLocalIdentifier(patient, identifier))
				continue;
			patient.getIdentifier().add(identifier);
		}
		return patient;
	}

	private boolean matchWithLocalIdentifier(Patient localPatient, Identifier identifier) {
		for (Identifier localIdentifier : localPatient.getIdentifier()) {
			if (localIdentifier.getValue().equals(identifier.getValue()))
				return true;
		}
		return false;
	}
}
