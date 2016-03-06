package de.slackspace.openkeepass.processor;

import java.util.ArrayList;
import java.util.List;

import de.slackspace.openkeepass.crypto.ProtectedStringCrypto;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.History;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.Property;
import de.slackspace.openkeepass.domain.PropertyValue;

public class ProtectedValueProcessor {

	public void processProtectedValues(ProtectionStrategy strategy, ProtectedStringCrypto protectedStringCrypto,
			KeePassFile keePassFile) {
		// Decrypt/Encrypt all protected values
		List<Entry> entries = keePassFile.getEntries();
		for (Entry entry : entries) {
			encryptOrDecryptProtectedValues(strategy, entry, protectedStringCrypto);

			// Also process historic password values
			History history = entry.getHistory();
			if (history != null) {
				for (Entry historicEntry : history.getHistoricEntries()) {
					encryptOrDecryptProtectedValues(strategy, historicEntry, protectedStringCrypto);
				}
			}
		}
	}

	private void encryptOrDecryptProtectedValues(ProtectionStrategy strategy, Entry entry, ProtectedStringCrypto protectedStringCrypto) {
		List<Property> removeList = new ArrayList<Property>();
		List<Property> addList = new ArrayList<Property>();

		List<Property> properties = entry.getProperties();
		for (Property property : properties) {
			handleProtectedValue(strategy, protectedStringCrypto, removeList, addList, property);
		}

		properties.removeAll(removeList);
		properties.addAll(addList);
	}

	private void handleProtectedValue(ProtectionStrategy strategy, ProtectedStringCrypto protectedStringCrypto, List<Property> removeList, List<Property> addList, Property property) {
		PropertyValue propertyValue = property.getPropertyValue();

		if (isPropertyValueProtected(propertyValue)) {
			String processedValue = strategy.apply(protectedStringCrypto, propertyValue.getValue());

			removeList.add(property);
			addList.add(new Property(property.getKey(), processedValue, property.isProtected()));
		}
	}

	private boolean isPropertyValueProtected(PropertyValue propertyValue) {
		return propertyValue.getValue() != null && !propertyValue.getValue().isEmpty()
				&& propertyValue.isProtected();
	}
}
