let CLASSROOM_ID = 1;

let CURRENT_SORT_COLUMN =
"recordedAt";

let CURRENT_SORT_DIRECTION =
"desc";

const REFRESH_INTERVAL =
5000;

const API_REPORT =
"http://localhost:8080/api/report";

const MANUAL_RETENTION_LIMIT =
11500;

const RETAIN_AFTER_CLEANUP =
8000;

// =====================================
// INIT
// =====================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        initializeEvents();

        initializeClock();

        loadReports();

        setInterval(() => {

            loadReports();

        }, REFRESH_INTERVAL);
    }
);

// =====================================
// EVENTS
// =====================================

function initializeEvents() {

    const classroomSelect =
        document.getElementById(
            "classroomSelect"
        );

    classroomSelect.addEventListener(
        "change",
        function () {

            CLASSROOM_ID =
                Number(this.value);

            updateRefreshStatus();

            loadReports();
        }
    );
}

// =====================================
// LIVE CLOCK
// =====================================

function initializeClock() {

    setInterval(() => {

        const currentTime =
            new Date().toLocaleTimeString();

        document.getElementById(
            "reportCurrentTime"
        ).innerText = currentTime;

    }, 1000);
}

// =====================================
// REFRESH STATUS
// =====================================

function updateRefreshStatus() {

    const indicator =
        document.getElementById(
            "reportsRefreshIndicator"
        );

    const currentTime =
        new Date().toLocaleTimeString();

    indicator.innerHTML = `

        <i class="ri-loader-4-line rotating"></i>

        Synced ${currentTime}
    `;
}

// =====================================
// LOAD REPORTS
// =====================================

async function loadReports() {

    try {

        updateRefreshStatus();

        await Promise.all([

            loadCounts(),

            loadReadings(),

            loadAlarms()
        ]);

    } catch (error) {

        console.error(
            "REPORT ERROR:",
            error
        );

        showNotification(
            "Failed loading report data",
            "error"
        );
    }
}

// =====================================
// LOAD COUNTS
// =====================================

async function loadCounts() {

    try {

        const res =
            await fetch(
                `${API_REPORT}/counts/${CLASSROOM_ID}`
            );

        const counts =
            await res.json();

        let status =
            "SAFE";

        let statusClass =
            "status-safe";

        let retentionInfo =
            `15000 / ${RETAIN_AFTER_CLEANUP}`;

        let retentionDesc =
            `Auto cleanup retain ${RETAIN_AFTER_CLEANUP} records`;

        if (
            counts.totalReadings > 12000 &&
            counts.totalReadings <= 15000
        ) {

            status =
            "APPROACHING LIMIT";

            statusClass =
            "status-warning";
        }

        else if (
            counts.totalReadings > 15000
        ) {

            status =
            "AUTO CLEANUP ACTIVE";

            statusClass =
            "status-danger";
        }

        document.getElementById(
            "reportInfo"
        ).innerHTML = `

            <div class="report-card modern-report-card">

                <div class="report-card-icon blue">

                    <i class="ri-database-2-line"></i>

                </div>

                <div class="report-card-content">

                    <span>
                        Total Readings
                    </span>

                    <h2>
                        ${formatNumber(
                            counts.totalReadings
                        )}
                    </h2>

                    <small>
                        Sensor records stored
                    </small>

                </div>

            </div>

            <div class="report-card modern-report-card">

                <div class="report-card-icon red">

                    <i class="ri-alarm-warning-line"></i>

                </div>

                <div class="report-card-content">

                    <span>
                        Total Alarms
                    </span>

                    <h2>
                        ${formatNumber(
                            counts.totalAlarms
                        )}
                    </h2>

                    <small>
                        Alarm triggers detected
                    </small>

                </div>

            </div>

            <div class="report-card modern-report-card">

                <div class="report-card-icon cyan">

                    <i class="ri-shield-check-line"></i>

                </div>

                <div class="report-card-content">

                    <span>
                        Database Status
                    </span>

                    <h2 class="${statusClass}">
                        ${status}
                    </h2>

                    <small>
                        Automatic retention monitoring
                    </small>

                </div>

            </div>

            <div class="report-card modern-report-card">

                <div class="report-card-icon green">

                    <i class="ri-refresh-line"></i>

                </div>

                <div class="report-card-content">

                    <span>
                        Retention Policy
                    </span>

                    <h2>
                        ${retentionInfo}
                    </h2>

                    <small>
                        ${retentionDesc}
                    </small>

                </div>

            </div>
        `;

    } catch (error) {

        console.error(
            "COUNT ERROR:",
            error
        );
    }
}

// =====================================
// LOAD READINGS
// =====================================

async function loadReadings() {

    const loading =
        document.getElementById(
            "readingLoading"
        );

    loading.classList.add("show");

    try {

        const res =
            await fetch(
                `${API_REPORT}/latest-readings/${CLASSROOM_ID}`
            );

        let readings =
            await res.json();

        readings.sort((a, b) => {

            let valA =
                a[CURRENT_SORT_COLUMN];

            let valB =
                b[CURRENT_SORT_COLUMN];

            if (valA === null)
                return 1;

            if (valB === null)
                return -1;

            if (
                CURRENT_SORT_DIRECTION === "asc"
            ) {

                return valA > valB
                    ? 1
                    : -1;
            }

            return valA < valB
                ? 1
                : -1;
        });

        const table =
            document.getElementById(
                "readingTable"
            );

        table.innerHTML = `

            <thead>

                <tr>

                    <th onclick="sortBy('id')">

                        ID ${getArrow('id')}

                    </th>

                    <th onclick="sortBy('dbLevel')">

                        dB ${getArrow('dbLevel')}

                    </th>

                    <th onclick="sortBy('modeAtTime')">

                        Mode ${getArrow('modeAtTime')}

                    </th>

                    <th onclick="sortBy('thresholdAtTime')">

                        Threshold ${getArrow('thresholdAtTime')}

                    </th>

                    <th onclick="sortBy('recordedAt')">

                        Time ${getArrow('recordedAt')}

                    </th>

                </tr>

            </thead>

            <tbody>

                ${generateReadingRows(readings)}

            </tbody>
        `;

    } catch (error) {

        console.error(
            "READINGS ERROR:",
            error
        );

        showTableError(
            "readingTable",
            "Failed loading sensor readings"
        );

    } finally {

        loading.classList.remove(
            "show"
        );
    }
}

// =====================================
// GENERATE READING ROWS
// =====================================

function generateReadingRows(readings) {

    if (!readings.length) {

        return `

            <tr>

                <td colspan="5">

                    No readings available

                </td>

            </tr>
        `;
    }

    return readings.map(r => `

        <tr>

            <td>${r.id}</td>

            <td>

                ${r.dbLevel}

                dB

            </td>

            <td>

                ${r.modeAtTime || '-'}

            </td>

            <td>

                ${r.thresholdAtTime || '-'}

            </td>

            <td>

                ${formatDate(r.recordedAt)}

            </td>

        </tr>

    `).join("");
}

// =====================================
// LOAD ALARMS
// =====================================

async function loadAlarms() {

    const loading =
        document.getElementById(
            "alarmLoading"
        );

    loading.classList.add("show");

    try {

        const res =
            await fetch(
                `${API_REPORT}/latest-alarms/${CLASSROOM_ID}`
            );

        const alarms =
            await res.json();

        const table =
            document.getElementById(
                "alarmTable"
            );

        table.innerHTML = `

            <thead>

                <tr>

                    <th>ID</th>

                    <th>Actual dB</th>

                    <th>Threshold</th>

                    <th>Mode</th>

                    <th>Time</th>

                </tr>

            </thead>

            <tbody>

                ${generateAlarmRows(alarms)}

            </tbody>
        `;

    } catch (error) {

        console.error(
            "ALARM ERROR:",
            error
        );

        showTableError(
            "alarmTable",
            "Failed loading alarm logs"
        );

    } finally {

        loading.classList.remove(
            "show"
        );
    }
}

// =====================================
// GENERATE ALARM ROWS
// =====================================

function generateAlarmRows(alarms) {

    if (!alarms.length) {

        return `

            <tr>

                <td colspan="5">

                    No alarm logs available

                </td>

            </tr>
        `;
    }

    return alarms.map(a => `

        <tr>

            <td>${a.id}</td>

            <td>

                ${a.actualDb} dB

            </td>

            <td>

                ${a.triggeredThreshold}

            </td>

            <td>

                ${a.modeAtTime}

            </td>

            <td>

                ${formatDate(a.triggeredAt)}

            </td>

        </tr>

    `).join("");
}

// =====================================
// SORT
// =====================================

function sortBy(column) {

    if (
        CURRENT_SORT_COLUMN === column
    ) {

        CURRENT_SORT_DIRECTION =
            CURRENT_SORT_DIRECTION === "asc"
            ? "desc"
            : "asc";

    } else {

        CURRENT_SORT_COLUMN =
            column;

        CURRENT_SORT_DIRECTION =
            "asc";
    }

    loadReadings();
}

// =====================================
// EXPORT
// =====================================

function exportData() {

    showNotification(
        "Preparing export report...",
        "info"
    );

    window.open(
        `${API_REPORT}/export/${CLASSROOM_ID}`
    );
}

// =====================================
// EXPORT + CLEAN
// =====================================

async function exportAndClean() {

    try {

        const res =
            await fetch(
                `${API_REPORT}/counts/${CLASSROOM_ID}`
            );

        const counts =
            await res.json();

        if (
            counts.totalReadings <
            MANUAL_RETENTION_LIMIT
        ) {

            alert(

                `Manual cleanup hanya dapat dilakukan jika total data >= ${MANUAL_RETENTION_LIMIT}.\n\n` +

                `Data saat ini: ${counts.totalReadings}`

            );

            return;
        }

        const confirmAction =
            confirm(

                `Total data saat ini ${counts.totalReadings}.\n\n` +

                `Sistem akan export dan menyimpan ${RETAIN_AFTER_CLEANUP} data terakhir untuk training.\n\n` +

                `Lanjutkan cleanup?`
            );

        if (!confirmAction)
            return;

        showNotification(
            "Preparing export cleanup...",
            "warning"
        );

        window.open(
            `${API_REPORT}/export-and-clean/${CLASSROOM_ID}`
        );

    } catch (error) {

        console.error(
            "EXPORT CLEAN ERROR:",
            error
        );

        alert(
            "Terjadi kesalahan saat validasi cleanup."
        );
    }
}

// =====================================
// FORMAT DATE
// =====================================

function formatDate(dateString) {

    const date =
        new Date(dateString);

    return date.toLocaleString();
}

// =====================================
// FORMAT NUMBER
// =====================================

function formatNumber(number) {

    return new Intl.NumberFormat().format(
        number
    );
}

// =====================================
// SHOW TABLE ERROR
// =====================================

function showTableError(
    tableId,
    message
) {

    document.getElementById(
        tableId
    ).innerHTML = `

        <tr>

            <td colspan="5">

                ${message}

            </td>

        </tr>
    `;
}

// =====================================
// NOTIFICATION
// =====================================

function showNotification(
    message,
    type
) {

    console.log(
        `[${type.toUpperCase()}] ${message}`
    );
}

// =====================================
// ARROW
// =====================================

function getArrow(column) {

    if (
        CURRENT_SORT_COLUMN !== column
    ) return "";

    return CURRENT_SORT_DIRECTION === "asc"
        ? "▲"
        : "▼";
}