package de.unimuenster.imi.randimi.repository;

import java.util.List;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclSidRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;

/**
 * @author Daniel Preciado-Marquez
 */
@WithUserDetails(value = "admin", userDetailsServiceBeanName = "randimiUserDetailsService")
public abstract class RepositoryTestBase extends RandimiIntegrationTest {

	@Autowired
	protected AclClassRepository aclClassRepository;

	@Autowired
	protected AclSidRepository aclSidRepository;

	@Autowired
	protected RandimiUserRepository randimiUserRepository;

	@Autowired
	protected StudyRepository studyRepository;

	private boolean setup = false;

	protected Study activeStudy;
	protected Study inactiveStudy;
	protected Study preGenereatedStudy;

	protected RandimiUser admin;
	protected RandimiUser activeUser;
	protected RandimiUser apiUser;
	protected RandimiUser inactiveUser;

	protected AclSid adminAclSid;

	protected AclClass aclClassStudy;

	@BeforeEach
	public void setup() {
		if (!setup) {

			List<Study> allStudies = studyRepository.findAll();
			for (Study study : allStudies) {
				switch (study.getGuiName()) {
					case "Active Study":
						activeStudy = study;
						break;
					case "Inactive Study":
						inactiveStudy = study;
						break;
					case "Pre-Generated Study":
						preGenereatedStudy = study;
						break;
				}
			}

			admin = randimiUserRepository.findFirstByUsernameIgnoreCase("ADMIN");
			activeUser = randimiUserRepository.findFirstByUsernameIgnoreCase("ACTIVE_TEST_USER");
			apiUser = randimiUserRepository.findFirstByUsernameIgnoreCase("API_TEST_USER");
			inactiveUser = randimiUserRepository.findFirstByUsernameIgnoreCase("INACTIVE_TEST_USER");

			adminAclSid = admin.getAclSid();

			aclClassStudy = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(),
			                                                                 Study.class.getName());

			setup = true;
		}
	}

}
