package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Version 1 of the coin toss algorithm. Only kept for studies created with this version.
 * <p>
 * The study arm is drawn uniformly among all study arms, so the ratios of the study arms are ignored.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class CoinTossRandomizationV1 extends CoinTossRandomization {

	@Autowired
	public CoinTossRandomizationV1(final MessageService messageService, final RandomService randomService) {
		super(messageService, randomService);
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	protected int drawStudyArmIndex(Site site, SubjectList subjectList) {
		return randomService.nextRandomInt(site, subjectList, site.getStudy().getStudyArms().size());
	}
}
