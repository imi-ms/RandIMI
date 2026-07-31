'use strict';

/**
 * Initializes the warnings for cross-site permissions.
 */
function initAllSitePermissions() {
    const checkboxes = document.querySelectorAll('[data-user-s]');
    for (const checkbox of checkboxes) {
        onAllSitePermissionsChange(checkbox);
    }
}

/**
 * Toggles the warning for cross-site permissions.
 *
 * @param checkbox {HTMLInputElement} Checkbox toggling the cross-site permission.
 */
function onAllSitePermissionsChange(checkbox) {
    const userId = checkbox.dataset.userS;
    const permission = checkbox.dataset.permission;

    const selector = '[data-user-t="' + userId + '"][data-permission="' + permission + '"]';
    const elements = document.querySelectorAll(selector);
    for (const element of elements) {
        element.style.display = checkbox.checked ? '' : 'none';
    }
}
