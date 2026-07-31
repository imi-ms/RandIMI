package de.unimuenster.imi.randimi.dto.study.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class AddStudyUsersDTO {
	@Getter @Setter
	private List<Long> newUserIds = new ArrayList<>();
}
