package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.RandimiUser;

import java.util.List;

/**
 * @author Daniel Preciado-Marquez
 */
public interface RandimiUserCustomRepository {

	boolean doesUsernameAlreadyExist(String username, long id);

	boolean isUserActive(long id);

	List<RandimiUser> getObsoleteUsers();

	List<RandimiUser> getNotifiedUsersOfStudy(Study study);

}
