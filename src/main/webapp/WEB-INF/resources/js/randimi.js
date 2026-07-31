'use strict';

(function () {
    /**
     * Container for all global randimi helper functions.
     */
    const randimi = {};

    /**
     * This field is used by {@link randimi.preventUnloadAfterChange} to track if the bound form has been changed.
     * @type {boolean}
     */
    randimi.modifiedForm = false;
    /**
     * Whether the next onbeforeunload event should be allowed.
     * @type {boolean}
     */
    randimi.allowNextUnload = false;

    randimi.dataTables = [];
    randimi.dataTablesForm = null;

    /**
     * Adds event listener for highlighting to the given test input for pseudonym regex.
     * @param {jQuery} target Input of the test value for the pseudonym regex.
     */
    randimi.addHighlightRegexTest = function (target) {
        if (target.val())
            randimi.highlightRegexTest(target);

        target.on('input', event => {
            randimi.highlightRegexTest($(event.target));
        });

        target.parent().siblings('input').on('input', event => {
            let id = event.target.id;
            id = id.replace('.', '\\.');
            randimi.highlightRegexTest($('#' + id + 'TestInput'));
        });
    }

    /**
     * Adds highlighting to the given test input for pseudonym regex.
     * @param {jQuery} target Input of the test value for the pseudonym regex.
     */
    randimi.highlightRegexTest = function (target) {
        const testValue = target.val();
        const regexString = target.parent().siblings('input').val();

        let matches;
        try {
            const regex = new RegExp(regexString);
            matches = testValue.match(regex);
        } catch (e) {
            matches = null;
        }

        const highlightDiv = target.siblings('div');
        if (matches === null) {
            highlightDiv.children('.randimi-decline-1').html(testValue);
            highlightDiv.children('.randimi-accept').html("");
            highlightDiv.children('.randimi-decline-2').html("");
        } else {
            const decline = testValue.split(matches[0]);
            if (decline.length > 2)
                for (let i = 2; i < decline.length; ++i)
                    decline[1] += matches[0] + decline[i];

            highlightDiv.children('.randimi-decline-1').html(decline[0]);
            highlightDiv.children('.randimi-accept').html(matches[0]);
            highlightDiv.children('.randimi-decline-2').html(decline[1]);
        }
    }

    /**
     * Initializes the autocomplete functionality for autocomplete container.
     *
     * @param targetId The ID of the autocomplete container div.
     * @param onSelect Callback function to be called when an item is selected.
     * @param onDeselect Callback function to be called when an item is deselected.
     */
    randimi.initAutocomplete = function (targetId, onSelect, onDeselect) {
        const autoComplete = randimiAutoComplete(targetId);
        if (onSelect != null) {
            autoComplete.onSelect.push(onSelect);
        }
        if (onDeselect != null) {
            autoComplete.onDeselect.push(onDeselect);
        }
        return autoComplete;
    }

    randimi.initChangeReason = function () {
        // Choose right option in the change reason select
        const changeReasonInput = document.getElementById('changeReason');
        if (!changeReasonInput) {
            return;
        }

        let foundReason = false;
        $('.changeReasonSelect option').each(function () {
            if ($(this).val() === $('.changeReason').val()) {
                this.selected = true;
                foundReason = true;
            }
        });
        // If no value was found choose 'Custom'
        if (foundReason === false) {
            if (changeReasonInput.value === '') {
                $('.changeReason').val('');
            } else {
                $('.changeReasonSelect').val('CUSTOM');
                $('.changeReason').show();
                $('.changeReasonLabel').show();
            }
        }
    }

    randimi.initSwitch = function () {
        document.querySelectorAll('.randimi-switch').forEach(element => {
            element.addEventListener('keyup', event => {
                // 32 is Space
                if (event.keyCode === 32) {
                    element.click();
                }
            });

            element.addEventListener('click', _ => {
                // TODO set focus
                const input = element.querySelector('input');
                input.click();
            });
        });
    }

    /**
     * Adds an event listener to all textual inputs that trims the value.
     */
    randimi.initTrimming = function () {
        document.querySelectorAll("input[type='password'], input[type='text'], textarea").forEach(input => {
           input.addEventListener('change', event => {
               input.value = input.value.trim();
           })
        });
    }

    /**
     * Sets the value of {@link randimi.modifiedForm} to true and makes the warning on top of the page visible.
     * This value is used by {@link randimi.preventUnloadAfterChange} to track if the bound form has been changed.
     */
    randimi.onFormModification = function () {
        randimi.modifiedForm = true;
        const modifiedInput = document.getElementById('modified');
        if (modifiedInput) {
            modifiedInput.value = true;
        }
        document.getElementById('unsavedChangesDiv').style.display = 'block';
    }

    /**
     * Asks for confirmation on unloading if the given form has been modified.
     * @param {string} formId Id of the form.
     * @param {string[]} excludedTargetIds Ids of elements, that should not trigger the confirmation.
     * @param {boolean} [modified=false] If the form is modified initially.
     * @param {string[]} [buttons=[]] Class name of buttons that should mark the form as modified on click.
     */
    randimi.preventUnloadAfterChange = function (formId, excludedTargetIds, modified, buttons) {
        buttons ??= [];

        if (modified) {
            randimi.onFormModification();
        }

        $('#' + formId).change(event => {
            if (!event.target.classList.contains('randimi-ignore-changes')) {
                randimi.onFormModification();
            }
        });

        for (const ex of excludedTargetIds) {
            document.getElementById(ex).addEventListener('click', () => {
                randimi.allowNextUnload = true;
            });
        }

        for (const className of buttons) {
            const button = document.getElementsByClassName(className);
            for (const b of button) {
                b.addEventListener('click', () => {
                    randimi.onFormModification();
                });
            }
        }

        window.onbeforeunload = event => {
            if (randimi.allowNextUnload) {
                randimi.allowNextUnload = false;
                return;
            }
            if (randimi.modifiedForm) {
                event.preventDefault();
            }
        }
    }

    /**
     * Sets the value of {@link randimi.modifiedForm}.
     * This value is used by {@link randimi.preventUnloadAfterChange} to track if the bound form has been changed.
     * If the value is true, the warning will be triggered.
     * @param {boolean} modified The new value of {@link randimi.modifiedForm}.
     */
    randimi.setModifiedForm = function (modified) {
        if (modified)
            randimi.onFormModification();
        else
            randimi.modifiedForm = false;
    }

    /**
     * Initializes a datatable asynchronously.
     * @param {string} tableId Id of the table to set up.
     * @param {Object} [opts={}] Optional options for datatables.
     * @param {function(Object): void} [func] Optional function that gets called after the initialization.
     */
    randimi.setUpDatatable = async function (tableId, opts, func) {
        const table = $('#' + tableId)
        if (!table.length) {
            return;
        }

        opts ??= {};
        func ??= dataTable => {
        };

        const langUrl = randimiConstants.contextPath + "/resources/datatables/datatables_" + randimiConstants.lang + ".json";

        if (opts.language === undefined) {
            opts.language = {url: langUrl};
        } else {
            const data = await $.ajax({
                dataType: "json",
                url: langUrl,
                data: ""
            });

            for (const [key, value] of Object.entries(opts.language))
                data[key] = value;
            opts.language = data;
        }

        opts.stateSave = true;
        opts.stateSaveCallback = function (settings, data) {
            const key = `randimi_v${randimiConstants.appVersion}-DataTables_${tableId}_${window.location.pathname}`;
            localStorage.setItem(key, JSON.stringify(data));
        };
        opts.stateLoadCallback = function (settings) {
            const key = `randimi_v${randimiConstants.appVersion}-DataTables_${tableId}_${window.location.pathname}`;
            return JSON.parse(localStorage.getItem(key));
        };

        if ($.fn.dataTable.isDataTable('#' + tableId)) {
            table.DataTable().clear().destroy();
        }
        const dataTable = table.DataTable(opts);
        func(dataTable);

        const tableElement = document.getElementById(tableId);
        const formElement = tableElement.closest('form');

        if (formElement !== null) {
            randimi.dataTablesForm = formElement;
            randimi.dataTables.push(dataTable);

            const buttons = formElement.querySelectorAll('button[type="submit"]');
            for (const button of buttons) {
                button.addEventListener('click', () => {
                   randimi.submitDatatables();
                });
            }
        }

        return dataTable;
    }

    /**
     * Must be called before submitting a datatable which contains inputs.
     * Clears the filter and disables pagination.
     * @param {string} formId If of the form element
     * @param {string[]} tableIds Id of the table element
     */
    randimi.submitDatatables = function () {
        if (randimi.dataTables.length === 0) {
            return;
        }

        const originalForm = randimi.dataTablesForm;
        //const originalForm = document.getElementById(formId);
        const clone = originalForm.cloneNode(true);
        originalForm.parentElement.appendChild(clone)
        //document.getElementById('formCopy').appendChild(clone);
        originalForm.classList.add("d-none");

        for (const table of randimi.dataTables) {
            table.search('').columns().search('').page.len(-1).draw();
        }
    }

    /**
     * Disables a checkbox.
     * @param {string} fieldName Id of the checkbox.
     * @param {string | null} value Value of the checkbox, if applicable.
     */
    randimi.disableCheckbox = function (fieldName, value = null) {
        const id = value === null ? fieldName : fieldName + "_" + value;
        const dummyId = id + "Dummy";
        const actualCheckbox = document.getElementById(id);
        const dummyCheckbox = document.getElementById(dummyId);

        dummyCheckbox.style.display = "block";
        actualCheckbox.style.display = "none";
        dummyCheckbox.checked = actualCheckbox.checked ?? false;
    };

    randimi.disableSelect = function (fieldName) {
        const dummyId = fieldName + "Dummy";
        const actualSelect = document.getElementById(fieldName);
        const dummySelect = document.getElementById(dummyId);

        dummySelect.style.display = "block";
        actualSelect.style.display = "none";
        dummySelect.value = actualSelect.value;
    }

    /**
     * Toggles the visibility of the content in a fieldset.
     * @param {jQuery} fieldset The fieldset, of which the content should be toggled.
     * @param {boolean} [visible=undefined] Indicates whether the content should (true) be shown or hidden (false).
     */
    randimi.toggleFieldset = function (fieldset, visible) {
        fieldset = $(fieldset);
        if (visible === undefined) {
            fieldset.siblings().slideToggle('slow');
            fieldset.children('.fa-eye').toggle();
            fieldset.children('.fa-eye-slash').toggle();
        } else if (visible) {
            fieldset.siblings().slideDown('slow');
            fieldset.children('.fa-eye').show();
            fieldset.children('.fa-eye-slash').hide();
        } else {
            fieldset.siblings().slideUp('slow');
            fieldset.children('.fa-eye').hide();
            fieldset.children('.fa-eye-slash').show();
        }
    }

    /**
     * Toggles the visibility of a password.
     * @param targetElement Input element.
     */
    randimi.togglePasswordVisibility = function (targetElement) {
        const targetId = targetElement.id;
        const targetIcon = document.getElementById("i-" + targetId);
        if (targetElement.type === 'password') {
            targetElement.type = 'text';
            targetIcon.classList.remove('fa-eye-slash');
            targetIcon.classList.add('fa-eye');
        } else {
            targetElement.type = 'password';
            targetIcon.classList.add('fa-eye-slash');
            targetIcon.classList.remove('fa-eye');
        }
    }

    /**
     * Updates the value of the change reason input when the change reason select is changed.
     * @param selectInput {HTMLSelectElement} The select input element of the change reason.
     */
    randimi.updateChangeReason = function (selectInput) {
        const changeReasonInput = selectInput.parentElement.querySelector('.changeReason');
        const changeReasonLabel = selectInput.parentElement.querySelector('.changeReasonLabel');

        if (selectInput.value === "CUSTOM") {
            changeReasonInput.value = "";
            changeReasonInput.style.display = "block";
            changeReasonInput.classList.remove('notToggleable');

            changeReasonLabel.style.display = "block";
            changeReasonLabel.classList.remove('notToggleable');
        } else {
            changeReasonInput.value = selectInput.value;
            changeReasonInput.style.display = "none";
            changeReasonInput.classList.add('notToggleable');

            changeReasonLabel.style.display = "none";
            changeReasonLabel.classList.add('notToggleable');
        }
    }

    window.randimi = randimi;
})();

$(document).ready(function () {
    // Adds to all legends in a fieldset buttons to toggle the visibility of the fieldset.
    randimi.initChangeReason();
    // Trims all inputs after the value is changed
    randimi.initTrimming();

    // Adds event listeners to all switch elements
    randimi.initSwitch();

    // Ignore page size select when tracking changes
    if ($.fn.dataTable) {
        $.fn.dataTable.ext.classes.sLengthSelect = 'randimi-ignore-changes';
    }
});

/**
 * Initializes auto-complete feature.
 * @param id The ID of the wrapper element.
 */
function randimiAutoComplete(id) {
    const randimiAutocomplete = {};

    /**
     * The ID of the wrapper element.
     * @type string
     */
    randimiAutocomplete.targetId = id;

    /**
     * If this autocomplete has been initialized.
     * @type {boolean}
     */
    randimiAutocomplete.isInitialized = false;

    /**
     * The wrapper div.
     * @type {HTMLDivElement | null}
     */
    randimiAutocomplete.wrapperDiv = null;

    /**
     * The auto complete div.
     * @type {HTMLDivElement | null}
     */
    randimiAutocomplete.autocompleteDiv = null;

    /**
     * Div containing all suggestions.
     * @type {HTMLDivElement | null}
     */
    randimiAutocomplete.suggestions = null;

    /**
     * Callback function that receives a suggestion.
     * @name SuggestionCallback
     * @callback
     * @param {HTMLDivElement} suggestion The suggestion element.
     */

    /**
     * List of callback functions called if a suggestion is selected.
     * @type {SuggestionCallback[]}
     */
    randimiAutocomplete.onSelect = [];

    /**
     * List of callback functions called if a suggestion is deselected.
     * @type {SuggestionCallback[]}
     */
    randimiAutocomplete.onDeselect = [];

    /**
     * Returns the suggestion for the given type and value.
     * @param {string} type The type of the suggestion in `data-randimi-type`.
     * @param {string} value The value of the suggestion in `data-randimi-value`.
     * @returns {HTMLElement | null} The suggestion element or null if no matching suggestion could be found.
     */
    randimiAutocomplete.getSuggestion = function (type, value) {
        for (const suggestion of this.suggestions.children) {
            const suggestionType = suggestion.dataset['randimiType'];
            const suggestionValue = suggestion.dataset['randimiValue'];
            if (suggestionType === type && suggestionValue === value) {
                return suggestion;
            }
        }

        return null;
    }

    /**
     * Selects the given suggestion.
     * @param {HTMLElement} suggestion The suggestion HTML element.
     */
    randimiAutocomplete.select = function (suggestion) {
        const template = this.wrapperDiv.querySelector('.randimi-autocomplete-template .randimi-autocomplete-selected-item');
        const clone = template.cloneNode(true);
        clone.querySelector('.randimi-autocomplete-selected-item-value').innerHTML = suggestion.innerText;

        clone.querySelector('.randimi-autocomplete-selected-item-icon').addEventListener('click', _ => {
            this.deselect(suggestion);
        });

        const selected = this.wrapperDiv.querySelector('.randimi-autocomplete-selected');
        selected.appendChild(clone);

        for (const callback of this.onSelect) {
            callback(suggestion);
        }
    }

    /**
     * Deselects the given suggestion.
     * @param {HTMLElement} suggestion The suggestion.
     */
    randimiAutocomplete.deselect = function (suggestion) {
        let selectedItem = null;
        this.wrapperDiv.querySelectorAll('.randimi-autocomplete-selected-item-value').forEach(element => {
            if (element.innerHTML === suggestion.innerText) {
                selectedItem = element;
            }
        });
        if (selectedItem !== null) {
            selectedItem.closest('.randimi-autocomplete-selected-item').remove();
            for (const callback of this.onDeselect) {
                callback(suggestion);
            }
        }
    }

    /**
     * Updates the suggested items.
     * @param {Event} event Event of the search input.
     */
    randimiAutocomplete.update = function (event) {
        const filterTerm = event.target.value.trim().toLowerCase();

        const selected = [...this.wrapperDiv.querySelectorAll('.randimi-autocomplete-selected .randimi-autocomplete-selected-item-value')].map(e => e.innerText.trim().toLowerCase());

        let fountOption = false;

        for (const suggestion of this.suggestions.children) {
            const option = suggestion.innerText.trim().toLowerCase();
            if (option.includes(filterTerm) && !selected.includes(option)) {
                fountOption = true;
                suggestion.style.display = '';
            } else {
                suggestion.style.display = 'none';
            }
        }

        if (filterTerm === '' || !fountOption) {
            this.autocompleteDiv.style.display = 'none';
        } else {
            this.autocompleteDiv.style.display = '';
        }
    }

    /**
     * Initializes the auto complete.
     * @param {string} id ID of the wrapper element.
     */
    randimiAutocomplete.init = function (id) {
        if (this.isInitialized) {
            console.warn("Autocomplete container with id " + id + " is already initialized.");
            return;
        }

        this.wrapperDiv = document.getElementById(id);

        if (!this.wrapperDiv) {
            console.error("Failed to initialize autocomplete container with id " + id + ". Element not found.")
            return;
        }

        const input = this.wrapperDiv.querySelector('input');

        this.autocompleteDiv = this.wrapperDiv.querySelector('.randimi-autocomplete');
        this.autocompleteDiv.style.display = 'none';

        this.suggestions = this.wrapperDiv.querySelector('.randimi-autocomplete-suggestions');
        for (const suggestion of this.suggestions.children) {
            suggestion.addEventListener('click', _ => {
                this.autocompleteDiv.style.display = 'none';
                input.value = "";

                this.select(suggestion);
            });
        }

        input.addEventListener('input', event => {
            this.update(event);
        });
        input.addEventListener('focus', event => {
            this.update(event);
        });

        window.addEventListener('click', event => {
            if (event.target.closest('.randimi-autocomplete-container') !== this.wrapperDiv) {
                this.autocompleteDiv.style.display = 'none';
            }
        });
        window.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                this.autocompleteDiv.style.display = 'none';
            }
        });

        this.isInitialized = true;
    }

    randimiAutocomplete.init(id);

    return randimiAutocomplete;
}
