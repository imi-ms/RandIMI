package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.controller.helper.RandimiHelper;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import org.springframework.stereotype.Service;

@Service
public class RandomService {

	public int nextRandomInt(final Site site, final SubjectList subjectList, final int bound) {
		if (site.getStudy().isActive()) {
			return site.nextRandomInt(bound);
		} else {
			return RandimiHelper.getRandom(subjectList.getId(), subjectList.size()).nextInt(bound);
		}
	}
}
