'use strict';

/**
 * @typedef SiteDTO
 * @type {object}
 * @property {number | null} capacity The capacity of the site.
 * @property {string} guiName The display name of the site.
 */

/**
 * @typedef StratumDTO
 * @type {object}
 * @property {string} apiId
 * @property {StratumPartBaseDTO[]} stratumParts
 */

/**
 * @typedef StratumPartBaseDTO
 * @type {object}
 * @property {string} apiId
 * @property {string} guiName
 */

/**
 * @typedef SubjectListStatisticsDTO
 * @type {object}
 * @property {StratumPartBaseDTO[]} stratumParts
 */

/**
 * @typedef SubjectSeriesDTO
 * @type {object}
 * @property {number} start Start date of the series.
 * @property {number} end End date of the series.
 * @property {number[]} series The series data.
 * @property {number} target The target value.
 * @property {SiteDTO | null} site The site the series belong to. Null if the series is not site-specific.
 * @property {Record<string, string> | null} strataParts The stratum parts the series belongs to.
 */

/**
 * If the chart is initialized.
 * @type {boolean}
 */
let isSiteChartInitialized = false;
let isStrataChartInitialized = false;

/**
 *
 * @type {{siteStatisticsTable: {selectedStudyArms: string[], selectedSites: string[]}, strataStatisticsTable: {selectedStudyArms: string[], selectedStratumParts: Record<string, string[]>}}}
 */
const selectedFilter = {
    siteStatisticsTable: {
        selectedStudyArms: [],
        selectedSites: [],
    },
    strataStatisticsTable: {
        selectedStudyArms: [],
        selectedStratumParts: {},
    }
}

/**
 * Initialize the page.
 */
function initPage() {
    randimi.initAutocomplete('strataStatisticsFilter', (element) => onSelect(element, 'strataStatisticsTable'), (element) => onDeselect(element, 'strataStatisticsTable'));
    randimi.initAutocomplete('siteStatisticsFilter', element => onSelect(element, 'siteStatisticsTable'), element => onDeselect(element, 'siteStatisticsTable'));
    openSiteStatistics();
}

/**
 * Shows the site statistics.
 */
function openSiteStatistics() {
    initSiteChart();
    openStatistics('siteStatistics');
}

/**
 * Shows the strata statistics.
 */
function openStrataStatistics() {
    initStrataChart();
    openStatistics('strataStatistics');
}

/**
 * Opens the statistics div.
 * @param {'siteStatistics' | 'strataStatistics'} divId ID of the div to open.
 */
function openStatistics(divId) {
    // Change visibility of the divs
    document.getElementById('siteStatistics').style.display = 'none';
    document.getElementById('strataStatistics').style.display = 'none';

    document.getElementById(divId).style.display = '';

    // Change highlight of the buttons
    const siteStatisticsButton = document.getElementById('siteStatisticsButton');
    siteStatisticsButton.classList.remove("randimi-sidebar-button-selected");

    const strataStatisticsButton = document.getElementById('strataStatisticsButton');
    strataStatisticsButton.classList.remove("randimi-sidebar-button-selected");

    const button = document.getElementById(divId + 'Button');
    button.classList.add("randimi-sidebar-button-selected");

    // Initialize accumulated numbers
    if (divId === "siteStatistics") {
        filterSites(divId + "Table");
        filterStudyArms(divId + "Table");
    } else {
        filterLists(divId + "Table");
        filterStudyArms(divId + "Table");
    }
}

/**
 * Initialize the site chart with data from the server.
 */
function initSiteChart() {
    if (isSiteChartInitialized) {
        return;
    }
    isSiteChartInitialized = true;

    const api = apiPath.replace('API_ID', studyApiId);

    const seriesPromises = [];
    seriesPromises.push(fetch(api));

    for (let i = 0; i < siteApiIds.length; i++) {
        const params = new URLSearchParams({ siteApiId: siteApiIds[i] });
        seriesPromises.push(fetch(api + "?" + params.toString()));
    }

    Promise.all(seriesPromises).then(value => {
        Promise.all(value.map(response => response.json())).then(subjectSeriesDTOs => {
            createChart(subjectSeriesDTOs, 'siteChart');
        });
    });
}

/**
 * Initialize the strata chart with data from the server.
 */
function initStrataChart() {
    if (isStrataChartInitialized) {
        return;
    }
    isStrataChartInitialized = true;

    const api = apiPath.replace('API_ID', studyApiId);

    const seriesPromises = [];
    seriesPromises.push(fetch(api));

    if (strata.length > 0) {
        for (let i = 0; i < subjectListStatistics.length; i++) {
            const subjectListStatistic = subjectListStatistics[i];

            const paramObject = {};
            for (let stratumIndex = 0; stratumIndex < strata.length; stratumIndex++) {
                const stratum = strata[stratumIndex];
                const part = subjectListStatistic.stratumParts[stratumIndex];
                paramObject["strataParameters." + stratum.apiId] = part.apiId;
            }

            if (siteStratum != null) {
                const last = subjectListStatistic.stratumParts.length - 1;
                paramObject['siteApiId'] = subjectListStatistic.stratumParts[last].apiId;
            }

            const params = new URLSearchParams(paramObject);
            seriesPromises.push(fetch(api + "?" + params.toString()));
        }
    }

    Promise.all(seriesPromises).then(value => {
        Promise.all(value.map(response => response.json())).then(subjectSeriesDTOs => {
            createChart(subjectSeriesDTOs, 'strataChart');
        });
    });
}

/**
 * Creates the chart with the given data.
 * @param {SubjectSeriesDTO[]} subjectsSeriesDTOs
 * @param {string} chartId ID of the chart element to initialize.
 */
function createChart(subjectsSeriesDTOs, chartId) {
    // Create x axis data
    const startDate = new Date(subjectsSeriesDTOs[0].start);
    const endDate = new Date(subjectsSeriesDTOs[0].end);

    const xAxisData = [];

    for (let d = startDate; d <= endDate; d.setDate(d.getDate() + 1)) {
        xAxisData.push(d.toISOString().split('T')[0]);
    }

    // Create series data
    const seriesData = [];
    const legendData = [];
    for (const subjectsSeriesDTO of subjectsSeriesDTOs) {
        let name;
        let z = 1;

        if (subjectsSeriesDTO.strataParts != null) {
           name = Object.values(subjectsSeriesDTO.strataParts).join(", ");
            if (subjectsSeriesDTO.site != null) {
                name += ", " + subjectsSeriesDTO.site.guiName;
            }
        } else if (subjectsSeriesDTO.site != null) {
            name = subjectsSeriesDTO.site.guiName;
        } else {
            name = chartId === 'strataChart' ? allStrataLabel : allSitesLabel;
            z = 2;
        }

        seriesData.push({
            name: name,
            type: 'line',
            data: subjectsSeriesDTO.series,
            markLine: {
                symbol: ['none', 'none'],
                label: {
                    color: mainTextColor,
                },
                data: [
                    {
                        yAxis: subjectsSeriesDTO.target,
                    }
                ]
            },
            z: z,
        });
        legendData.push(name);
    }

    // Specify the configuration items and data for the chart
    const option = {
        toolbox: {
          feature: {
              dataZoom: {
                  yAxisIndex: 'none'
              },
              saveAsImage: {show: true},
          }
        },
        textStyle: {
          color: mainTextColor,
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: legendData
        },
        xAxis: {
            data: xAxisData
        },
        yAxis: {
            min: 0,
            max: studyCapacity,
        },
        series: seriesData,
        dataZoom: [
            {
                type: 'inside',
                start: 0,
                end: 100,
            },
            {
                start: 0,
                end: 100
            }
        ]
    };

    // Initialize the echarts instance based on the prepared dom
    const chartElement = document.getElementById(chartId);
    const myChart = echarts.init(chartElement);

    const resizeObserver = new ResizeObserver(entries => {
       myChart.resize();
    });
    resizeObserver.observe(chartElement);

    // Display the chart using the configuration items and data just specified.
    myChart.setOption(option);

    // Set the initial state of the capacities
    toggleCapacities(chartId);
    toggleStack(chartId);
}

/**
 * Toggles the visibility of the capacities in the site chart.
 */
function toggleSiteChartCapacities() {
    if (!isSiteChartInitialized) {
        return;
    }
    toggleCapacities('siteChart');
}

/**
 * Toggles the visibility of the capacities in the strata chart.
 */
function toggleStrataChartCapacities() {
    if (!isStrataChartInitialized) {
        return;
    }
    toggleCapacities('strataChart');
}

/**
 * Toggles the visibility of the capacities for the chart with the given ID.
 * @param chartId The ID of the chart.
 */
function toggleCapacities(chartId) {
    const showCapacities = document.getElementById(chartId + "ShowCapacities").checked;
    const myChart = echarts.getInstanceByDom(document.getElementById(chartId));

    const seriesData = [];

    // Show/hide the line in all series
    for (const series of myChart.getOption().series) {
        seriesData.push({
            markLine: {
                silent: !showCapacities,
                label: {
                    show: showCapacities,
                },
                lineStyle: {
                    opacity: showCapacities ? 1 : 0,
                },
            }
        });
    }

    // Create the option object to be merged
    const option = {
        yAxis: {
            max: showCapacities ? studyCapacity : null,
        },
        series: seriesData,
    };

    myChart.setOption(option);
}

/**
 * Toggles stacking for the site chart.
 */
function toggleSiteChartStack() {
    if (!isSiteChartInitialized) {
        return;
    }
    toggleStack('siteChart');
}

/**
 * Toggles stacking for the strata chart.
 */
function toggleStrataChartStack() {
    if (!isStrataChartInitialized) {
        return;
    }
    toggleStack('strataChart');
}

/**
 * Toggles stacking for the chart with the given ID.
 * @param chartId The ID of the chart.
 */
function toggleStack(chartId) {
    const stack = document.getElementById(chartId + "Stack").checked;
    const myChart = echarts.getInstanceByDom(document.getElementById(chartId));

    const seriesData = [];

    for (const series of myChart.getOption().series) {
        if (series.name === allSitesLabel || series.name === allStrataLabel) {
            // Keep line for the accumulated series
            seriesData.push({
            });
        } else {
            seriesData.push({
                stack: stack ? 'Total' : null,
                areaStyle: stack ? {} : null,
                emphasis: {
                    focus: 'series',
                },
            });
        }
    }

    // Create the option object to be merged
    const option = {
        series: seriesData,
    };

    myChart.setOption(option);
}

function onSelect(element, tableId) {
    const type = element.dataset['randimiType'];

    if (type === 'study-arm') {
        const studyArm = element.dataset['randimiValue'];
        selectedFilter[tableId].selectedStudyArms.push(studyArm);

        filterStudyArms(tableId);
    } else if (type === 'stratum') {
        // Add the selected stratum part
        const stratumPart = element.dataset['randimiPartValue'];
        const stratum = element.dataset['randimiStratumValue'];
        if (selectedFilter[tableId].selectedStratumParts[stratum] === undefined) {
            selectedFilter[tableId].selectedStratumParts[stratum] = [];
        }
        selectedFilter[tableId].selectedStratumParts[stratum].push(stratumPart);

        filterLists(tableId);
    } else if (type === 'site') {
        const site= element.dataset['randimiValue'];
        selectedFilter[tableId].selectedSites.push(site);

        filterSites(tableId);
    }
}

function onDeselect(element, tableId) {
    const type = element.dataset['randimiType'];

    if (type === 'study-arm') {
        const studyArm = element.dataset['randimiValue'];
        selectedFilter[tableId].selectedStudyArms = selectedFilter[tableId].selectedStudyArms.filter(value => value !== studyArm);

        filterStudyArms(tableId);
    } else if (type === 'stratum') {
        // Remove the selected stratum part
        const stratumPart = element.dataset['randimiPartValue'];
        const stratum = element.dataset['randimiStratumValue'];
        selectedFilter[tableId].selectedStratumParts[stratum] = selectedFilter[tableId].selectedStratumParts[stratum].filter(value => value !== stratumPart);
        if (selectedFilter[tableId].selectedStratumParts[stratum].length === 0) {
            delete selectedFilter[tableId].selectedStratumParts[stratum];
        }

        filterLists(tableId);
    } else if (type === 'site') {
        const site= element.dataset['randimiValue'];
        selectedFilter[tableId].selectedSites = selectedFilter[tableId].selectedSites.filter(value => value !== site);

        filterSites(tableId);
    }
}

/**
 * Filter the table based on the selected study arms.
 * @param {string} tableId Id of the table to filter.
 */
function filterStudyArms(tableId) {
    const all = document.querySelectorAll('.randimi-statistics-column');

    if (selectedFilter[tableId].selectedStudyArms.length === 0) {
        // Show all
        all.forEach(element => {
            element.style.display = '';
        });
    } else {
        // Hide all
        all.forEach(element => {
            element.style.display = 'none';
        });

        // Show selected
        const querySelector = selectedFilter[tableId].selectedStudyArms.map(value => '[data-randimi-study-arm="' + value + '"]').join(',');
        const selected = document.querySelectorAll(querySelector);
        selected.forEach(element => {
            element.style.display = '';
        });
    }

    // Update accumulated numbers
    document.querySelectorAll(`#${tableId} [data-randimi-acc]`).forEach(element => {
        const rowIndex = element.dataset.randimiAcc;

        let sum = 0;
        let capacitySum = 0;
        document.querySelectorAll(`#${tableId} [data-randimi-row="${rowIndex}"]`).forEach(child => {
            if (child.style.display !== 'none') {
                const numbers = child.textContent.split(' / ');
                sum += parseInt(numbers[0]);
                capacitySum += parseInt(numbers[1]);
            }
        });

        element.textContent = sum + ' / ' + capacitySum;
    });
}

/**
 * Filter the table based on the selected stratum parts.
 * @param {string} tableId Id of the table to filter.
 */
function filterLists(tableId) {
    const all = document.querySelectorAll('.randimi-statistics-row');

    if (Object.keys(selectedFilter[tableId].selectedStratumParts).length === 0) {
        // Show all
        all.forEach(element => {
            element.style.display = '';
        });
    } else {
        // Hide all
        all.forEach(element => {
            element.style.display = 'none';
        });

        // Show lists that match the stratum parts
        let orParts = [];
        for (const [s, p] of Object.entries(selectedFilter[tableId].selectedStratumParts)) {
            // Join and parts
            if (orParts.length === 0) {
                p.forEach(value => {
                    orParts.push(`[data-randimi-stratum-part-${s}="${value}"]`);
                });
            } else {
                let newOrParts = [];

                p.forEach(value => {
                    for (const orPart of orParts) {
                        newOrParts.push(orPart + `[data-randimi-stratum-part-${s}="${value}"]`);
                    }
                });

                orParts = newOrParts;
            }
        }

        // Join or parts with commas
        let querySelector = orParts.join(',');

        // Show the selected rows
        const selected = document.querySelectorAll(querySelector);
        selected.forEach(element => {
            element.style.display = '';
        });
    }

    // Accumulate numbers
    accumulateColumns(tableId);

    // Apply filter to the chart
    filterStrataChart(tableId);
}

/**
 * Shows strata combinations in the chart based on the shown rows in the given table
 * @param tableId The ID of the strata table.
 */
function filterStrataChart(tableId) {
    const myChart = echarts.getInstanceByDom(document.getElementById('strataChart'));
    if (!myChart) {
        return;
    }

    if (Object.keys(selectedFilter[tableId].selectedStratumParts).length === 0) {
        myChart.dispatchAction({
            type: 'legendAllSelect',
        });
    } else {
        myChart.dispatchAction({
            type: 'legendAllSelect',
        });
        myChart.dispatchAction({
            type: 'legendInverseSelect',
        });
        myChart.dispatchAction({
            type: 'legendSelect',
            name: allStrataLabel,
        });

        for (const legend of myChart.getOption().legend[0].data) {
            let show = false;

            for (const stratum of Object.values(selectedFilter[tableId].selectedStratumParts)) {
                let isIncluded = false;

                for (const part of stratum) {
                    if (legend.split(', ').includes(part)) {
                        isIncluded = true;
                        show = true;
                        break;
                    }
                }

                if (!isIncluded) {
                    show = false;
                    break;
                }
            }

            if (show) {
                myChart.dispatchAction({
                    type: 'legendSelect',
                    name: legend,
                });
            }
        }
    }
}

/**
 * Filter the table based on the selected sites.
 * @param {string} tableId Id of the table to filter.
 */
function filterSites(tableId) {
    const all = document.querySelectorAll('.randimi-statistics-row');

    if (selectedFilter[tableId].selectedSites.length === 0) {
        // Show all
        all.forEach(element => {
            element.style.display = '';
        });
    } else {
        // Hide all
        all.forEach(element => {
            element.style.display = 'none';
        });

        // Join or parts with commas
        const querySelector = selectedFilter[tableId].selectedSites.map(value => '[data-randimi-site="' + value + '"]').join(',');

        // Show the selected rows
        const selected = document.querySelectorAll(querySelector);
        selected.forEach(element => {
            element.style.display = '';
        });
    }

    // Accumulate numbers
    accumulateColumns(tableId);

    // Apply filter to the chart
    filterSiteChart(tableId);
}

/**
 * Shows sites in the chart based on the selected sites
 * @param tableId The ID of the site table.
 */
function filterSiteChart(tableId) {
    const myChart = echarts.getInstanceByDom(document.getElementById('siteChart'));
    if (!myChart) {
        return;
    }

    if (selectedFilter[tableId].selectedSites.length === 0) {
        myChart.dispatchAction({
            type: 'legendAllSelect',
        });
    } else {
        myChart.dispatchAction({
            type: 'legendAllSelect',
        });
        myChart.dispatchAction({
            type: 'legendInverseSelect',
        });
        myChart.dispatchAction({
            type: 'legendSelect',
            name: allSitesLabel,
        });
        selectedFilter[tableId].selectedSites.forEach(site => {
            myChart.dispatchAction({
                type: 'legendSelect',
                name: site,
            });
        });
    }
}

/**
 * Accumulate the statistics of all rows for each column.
 * @param {string} tableId Id of the table.
 */
function accumulateColumns(tableId) {
    document.querySelectorAll(`#${tableId} [data-randimi-acc-study-arm]`).forEach(element => {
        const studyArm = element.dataset.randimiAccStudyArm;

        let sum = 0;
        let capacitySum = 0;
        document.querySelectorAll(`#${tableId} td[data-randimi-study-arm="${studyArm}"]:not([data-randimi-acc-study-arm])`).forEach(child => {
            if (child.parentElement.style.display !== 'none') {
                const numbers = child.textContent.split(' / ');
                sum += parseInt(numbers[0]);
                capacitySum += parseInt(numbers[1]);
            }
        });

        element.textContent = sum + ' / ' + capacitySum;
    });
}
