package de.unimuenster.imi.randimi.validator.subject;

import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class SubjectDTOValidatorTest extends ValidatorTestBase {

    private StudyRepository studyRepository;
    private SiteRepository siteRepository;
    private SubjectDTOValidator validator;

    private static final long ACTIVE = 1L;
    private static final long INACTIVE = 2L;
    private static final long INEXISTENT = 3L;

    @BeforeEach
    public void setup() {
        studyRepository = mock(StudyRepository.class);
        Study active = new Study();
        active.setId(ACTIVE);
        active.setActivationDate(new Timestamp(System.currentTimeMillis()));
        when(studyRepository.findById(ACTIVE)).thenReturn(Optional.of(active));

        siteRepository = mock(SiteRepository.class);
        // TODO: Implement mock stuff

        Study inactive = new Study();
        inactive.setId(INACTIVE);
        inactive.setActivationDate(null); // Not activated
        when(studyRepository.findById(INACTIVE)).thenReturn(Optional.of(inactive));

        validator = new SubjectDTOValidator(messageService, studyRepository, siteRepository);
    }

    @Test
    public void validateNonExistentStudy() {
        Errors errors = mock(Errors.class);
        SubjectDTO dto = new SubjectDTO();
        dto.setStudyId(INEXISTENT); // Not existent

        validator.validate(dto, errors);

        verify(errors).rejectValue("studyId", "errormessage", getMsg("validator.subject.studyNotExist", INEXISTENT));
    }

    @Test
    public void validateInactiveStudy() {
        Errors errors = mock(Errors.class);
        SubjectDTO dto = new SubjectDTO();
        dto.setStudyId(INACTIVE); // exists but not activated

        validator.validate(dto, errors);

        verify(errors).rejectValue("studyId", "errormessage", getMsg("validator.subject.studyNotActive", INACTIVE));
    }
}
