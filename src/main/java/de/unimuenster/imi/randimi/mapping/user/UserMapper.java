package de.unimuenster.imi.randimi.mapping.user;

import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.model.user.UserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {

    /**
     * Apply the changes from the given DTO to the user to edit.
     * Apply the changed roles according to the rights of the current user.
     * <p>
     * The fields invitedBy and invitedUsers are not set.
     *
     * @param userDTO     The userDTO containing the changes.
     * @param editedUser  The user to modify.
     * @param editingUser The current user making the modification.
     * @return The modified user.
     */
    public RandimiUser toUser(final UserDTO userDTO, RandimiUser editedUser, final RandimiUser editingUser) {
        // If the user does not exist, create a new one
        if (userDTO.getId() == 0) {
            editedUser = RandimiUser.newUser(userDTO.getUsername());
        }
        editedUser.setId(userDTO.getId());
        editedUser.setUsername(userDTO.getUsername());
        editedUser.setPassword(userDTO.getPassword());
        editedUser.setEnabled(userDTO.isEnabled());
        editedUser.setFirstName(userDTO.getFirstName());
        editedUser.setLastName(userDTO.getLastName());
        editedUser.setEMail(userDTO.getEMail());

        applyRoles(userDTO, editedUser, editingUser);

        return editedUser;
    }

    /**
     * Returns the {@link UserDTO} representation auf the user.
     *
     * @return {@link UserDTO} representation auf the user.
     */
    public UserDTO toUserDTO(RandimiUser user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setEnabled(user.isEnabled());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEMail(user.getEMail());
        userDTO.setInvitationToken(user.getInvitationToken());

        if (user.getInvitedByUsername() != null) {
            userDTO.setInvitedBy(user.getInvitedByUsername());
        }

        List<String> userRoleList = new ArrayList<>();
        for (UserRole userRole : user.getUserRoles()) {
            userRoleList.add(userRole.getEnumRole().getTextValue());
        }
        userDTO.setUserRoles(userRoleList);

        userDTO.setSkipEMailValidation(false);

        return userDTO;
    }

    /**
     * Apply the changed roles according to the rights of the current user.
     *
     * @param userDTO     The userDTO containing the changes.
     * @param editedUser  The user to modify.
     * @param editingUser The current user making the modification.
     */
    public void applyRoles(final UserDTO userDTO, final RandimiUser editedUser, final RandimiUser editingUser) {
        // Remove roles that the user can remove
        for (var role : UserRoles.values()) {
            if (canGrantRole(editingUser, role) ) {
                editedUser.removeUserRole(role);
            }
        }

        // Add only roles that the user is allowed to add
        for (String userRole : userDTO.getUserRoles()) {
            final UserRoles userRoleEnum = UserRoles.fromString(userRole);
            if (canGrantRole(editingUser, userRoleEnum)) {
                editedUser.addUserRole(new UserRole(userRoleEnum));
            }
        }
    }

    /**
     * Checks if the given user is allowed to grant the given role to another user.
     *
     * @param user The user granting the role.
     * @param role The role to grant.
     * @return True if the user is allowed to.
     */
    private boolean canGrantRole(final RandimiUser user, final UserRoles role) {
        return user.hasUserRole(UserRoles.ROLE_ADMIN) || user.hasUserRole(role);
    }
}
