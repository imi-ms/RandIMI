'use strict';

let subjectListsTable;
let allSubjectsTable;

const selectedFilter = {};

async function initPageSubjectLists() {
    subjectListsTable = await randimi.setUpDatatable('subjectListsTable', {
        'columnDefs': [
            {'targets': [numberStrata + numberSiteStrata], 'orderData': [numberStrata + numberSiteStrata + 1]},
            {'targets': [numberStrata + numberSiteStrata + 1], 'visible': false, 'searchable': false, 'type': 'num'},
            {'targets': [numberStrata + numberSiteStrata + 2], 'orderable': false}
        ],
    });

    const opts = {
        "columnDefs": [
            {
                "targets": [0, 1, 2, 3 + numberStrata, 4 + numberStrata, 5 + numberStrata],
                "type": "string",
            },
            {
                "targets": [4 + numberStrata, 5 + numberStrata],
                "orderable": false,
            },
            {
                "targets": [5 + numberStrata],
                "searchable": false,
            },
        ],
        order: [0, 'desc'],
    };
    allSubjectsTable = await randimi.setUpDatatable("allSubjectsTable", opts);

    const columnFilter = document.querySelectorAll('[data-randimi-column]');
    columnFilter.forEach((element) => {
        const column = parseInt(element.dataset['randimiColumn']);
        if (!selectedFilter[column]) {
            selectedFilter[column] = [];
        }
    });
    randimi.initAutocomplete('allSubjectsFilter', (element) => onSelect(element), (element) => onDeselect(element));

    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const tab = urlParams.get("tab") ?? 'subjectLists';

    setTimeout(() => {
        open(tab);
    }, 100);
}

function openSubjectLists() {
    open('subjectLists');
}

function openAllSubjects() {
    open('allSubjects');
}

/**
 * Opens the specified tab.
 * @param {'subjectLists'|'allSubjects'} tab
 */
function open(tab) {
    const tabNames = ["subjectLists", "allSubjects"];


    for (const tabName of tabNames) {
        document.getElementById(tabName).hidden = true;

        const buttonId = tabName + "Button";
        const buttonElement = document.getElementById(buttonId);
        buttonElement.disabled = false;
    }

    const buttonElement = document.getElementById(tab + 'Button');
    buttonElement.disabled = true;

    document.getElementById(tab).hidden = false;

    const url = new URL(window.location.href);
    url.searchParams.set('tab', tab);
    window.history.replaceState(null, null, url);

    if (tab === 'subjectLists') {
        subjectListsTable.columns.adjust();
    } else if (tab === 'allSubjects') {
        allSubjectsTable.columns.adjust();
    }
}

function onSelect(element) {
    const column = parseInt(element.dataset['randimiColumn']);
    const value = element.dataset['randimiValue'];
    selectedFilter[column].push(value);

    const searchTerm = selectedFilter[column].join('|');
    allSubjectsTable.columns(column).search(searchTerm, true, false).draw();
}

function onDeselect(element) {
    const column = parseInt(element.dataset['randimiColumn']);
    const value = element.dataset['randimiValue'];
    selectedFilter[column] = selectedFilter[column].filter(v => v !== value);

    const searchTerm = selectedFilter[column].join('|');
    allSubjectsTable.columns(column).search(searchTerm, true, false).draw();
}
