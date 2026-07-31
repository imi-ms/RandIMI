package de.unimuenster.imi.randimi.cronjob;

import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Cronjob that deletes users that have been invited more than 30 days ago but never accepted the invitation.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez <daniel.preciado.marquez@uni-muenster.de>
 */
@Service
public class DeleteObsolete {
	
	final RandimiUserRepository userRepository;

	@Autowired
	public DeleteObsolete(RandimiUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Deletes users that were invited more than 30 days ago.
	 * Gets called every day at 2:00 AM.
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	public void deleteObsoleteUsers() {
		List<RandimiUser> obsoleteUsers = userRepository.getObsoleteUsers();
		userRepository.deleteAll(obsoleteUsers);
	}
}
