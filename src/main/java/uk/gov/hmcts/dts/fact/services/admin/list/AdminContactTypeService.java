package uk.gov.hmcts.dts.fact.services.admin.list;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dts.fact.exception.DuplicatedListItemException;
import uk.gov.hmcts.dts.fact.exception.ListItemInUseException;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.model.admin.ContactType;
import uk.gov.hmcts.dts.fact.repositories.ContactRepository;
import uk.gov.hmcts.dts.fact.repositories.ContactTypeRepository;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class AdminContactTypeService {

    private final ContactTypeRepository contactTypeRepository;
    private final ContactRepository contactRepository;

    @Autowired
    public AdminContactTypeService(final ContactTypeRepository contactTypeRepository, final ContactRepository contactRepository) {
        this.contactTypeRepository = contactTypeRepository;
        this.contactRepository = contactRepository;

    }

    public List<ContactType> getAllContactTypes() {
        return contactTypeRepository.findAll()
            .stream()
            .map(ContactType::new)
            .sorted(Comparator.comparing(ContactType::getType))
            .collect(toList());
    }

    public ContactType getContactType(final Integer id) {
        try {
            return new ContactType(contactTypeRepository.getOne(id));
        } catch (final javax.persistence.EntityNotFoundException exception) {
            throw new NotFoundException(exception);
        }
    }

    @Transactional
    public ContactType createContactType(final ContactType contactType) {
        checkIfContactTypeAlreadyExists(contactType.getType());
        return new ContactType(contactTypeRepository.save(createNewContactTypeEntityFromModel(contactType)));
    }

    @Transactional
    public ContactType updateContactType(final ContactType updatedContactType) {
        uk.gov.hmcts.dts.fact.entity.ContactType contactTypeEntity =
            contactTypeRepository.findById(updatedContactType.getId())
                .orElseThrow(() -> new NotFoundException(updatedContactType.getId().toString()));

        checkIfUpdatedContactTypeAlreadyExists(updatedContactType);
        contactTypeEntity.setDescription(updatedContactType.getType());
        contactTypeEntity.setDescriptionCy(updatedContactType.getTypeCy());
        return new ContactType(contactTypeRepository.save(contactTypeEntity));
    }

    public void deleteContactType(final Integer contactTypeId) {
        checkContactTypeIsNotInUse(contactTypeId);

        try {
            contactTypeRepository.deleteById(contactTypeId);
        } catch (EmptyResultDataAccessException ex) {
            log.warn("Contact Type could not be deleted because it no longer exists: " + contactTypeId);
            throw new NotFoundException(ex);
        } catch (DataAccessException ex) {
            log.warn("A data access exception was thrown when trying to delete a contact type: " + contactTypeId);
            throw new ListItemInUseException(ex);
        }
    }

    private void checkIfContactTypeAlreadyExists(final String contactTypeName) {
        if (getAllContactTypes().stream().anyMatch(ct -> ct.getType().equalsIgnoreCase(contactTypeName))) {
            throw new DuplicatedListItemException("Contact Type already exists: " + contactTypeName);
        }
    }

    private void checkIfUpdatedContactTypeAlreadyExists(final ContactType updatedContactType) {

        final List<ContactType>  contactTypes =  getAllContactTypes();
        contactTypes.remove(updatedContactType);

        if (contactTypes.stream().anyMatch(ct -> ct.getType().equalsIgnoreCase(updatedContactType.getType()))) {
            throw new DuplicatedListItemException("Updated Contact Type already exists: " + updatedContactType.getType());
        }
    }

    private uk.gov.hmcts.dts.fact.entity.ContactType createNewContactTypeEntityFromModel(final ContactType contactType) {
        final uk.gov.hmcts.dts.fact.entity.ContactType entity = new uk.gov.hmcts.dts.fact.entity.ContactType();
        entity.setDescription(contactType.getType());
        entity.setDescriptionCy(contactType.getTypeCy());
        return entity;
    }

    private void checkContactTypeIsNotInUse(Integer contactTypeId) {
        if (!contactRepository.getContactsByAdminTypeId(contactTypeId).isEmpty()) {
            throw new ListItemInUseException(contactTypeId.toString());
        }
    }
}



