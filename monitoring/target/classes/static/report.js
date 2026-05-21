let CLASSROOM_ID = 1;

let CURRENT_SORT_COLUMN = "recordedAt";
let CURRENT_SORT_DIRECTION = "desc";

const API_REPORT =
    "http://localhost:8080/api/report";

// =====================================================
// INIT
// =====================================================
document.addEventListener(
    "DOMContentLoaded",
    () => {

        const classroomSelect =
            document.getElementById(
                "classroomSelect"
            );

        if (classroomSelect) {

            classroomSelect.addEventListener(
                "change",
                function () {

                    CLASSROOM_ID = this.value;

                    loadReports();
                }
            );
        }

        loadReports();

        // AUTO REFRESH 5 DETIK
        setInterval(() => {

            loadReports();

        }, 5000);
    }
);

// =====================================================
// LOAD ALL REPORTS
// =====================================================
async function loadReports() {

    await loadCounts();

    await loadReadings();

    await loadAlarms();

    const indicator =
        document.getElementById(
            "reportsRefreshIndicator"
        );

    if (indicator) {

        indicator.innerText =
            "Last updated: "
            + new Date().toLocaleTimeString();

        indicator.classList.add("show");

        indicator.classList.add("pulse");

        setTimeout(() => {

            indicator.classList.remove(
                "pulse"
            );

        }, 1000);
    }
}

// =====================================================
// LOAD COUNTS
// =====================================================
async function loadCounts() {

    const res =
        await fetch(
            `${API_REPORT}/counts/${CLASSROOM_ID}`
        );

    const counts =
        await res.json();

    let status = "SAFE";

    let statusColor = "green";

    // =====================================================
    // NEW LIMIT LOGIC
    // =====================================================
    if (
        counts.totalReadings > 12000 &&
        counts.totalReadings <= 15000
    ) {

        status = "APPROACHING LIMIT";

        statusColor = "orange";
    }
    else if (
        counts.totalReadings > 15000
    ) {

        status = "OVER LIMIT";

        statusColor = "red";
    }

    document.getElementById(
        "reportInfo"
    ).innerHTML = `

        <div class="card">
            <h3>Total Readings</h3>
            <p>${counts.totalReadings}</p>
        </div>

        <div class="card">
            <h3>Total Alarms</h3>
            <p>${counts.totalAlarms}</p>
        </div>

        <div class="card">
            <h3>Database Status</h3>

            <p style="
                color:${statusColor};
                font-weight:bold;
            ">
                ${status}
            </p>
        </div>

        <div class="card">
            <h3>Retention Policy</h3>
            <p>
                15000 Max / 8500 Retained
            </p>
        </div>
    `;
}

// =====================================================
// LOAD READINGS
// =====================================================
async function loadReadings() {

    const loading =
        document.getElementById(
            "readingLoading"
        );

    const table =
        document.getElementById(
            "readingTable"
        );

    if (loading)
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

            if (valA === null) return 1;
            if (valB === null) return -1;

            if (
                CURRENT_SORT_DIRECTION === "asc"
            ) {

                return valA > valB ? 1 : -1;

            } else {

                return valA < valB ? 1 : -1;
            }
        });

        table.innerHTML = `

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
        `;

        readings.forEach(r => {

            table.innerHTML += `

                <tr>
                    <td>${r.id}</td>
                    <td>${r.dbLevel}</td>
                    <td>${r.modeAtTime || '-'}</td>
                    <td>${r.thresholdAtTime || '-'}</td>
                    <td>${r.recordedAt}</td>
                </tr>
            `;
        });

    } catch (error) {

        console.error(
            "Error loading readings:",
            error
        );
    }

    if (loading)
        loading.classList.remove("show");
}

// =====================================================
// LOAD ALARMS
// =====================================================
async function loadAlarms() {

    const loading =
        document.getElementById(
            "alarmLoading"
        );

    if (loading)
        loading.classList.add("show");

    const res =
        await fetch(
            `${API_REPORT}/latest-alarms/${CLASSROOM_ID}`
        );

    let alarms =
        await res.json();

    if (loading)
        loading.classList.remove("show");

    const table =
        document.getElementById(
            "alarmTable"
        );

    table.innerHTML = `

        <tr>
            <th>ID</th>
            <th>Actual dB</th>
            <th>Threshold</th>
            <th>Mode</th>
            <th>Time</th>
        </tr>
    `;

    alarms.forEach(a => {

        table.innerHTML += `

            <tr>
                <td>${a.id}</td>
                <td>${a.actualDb}</td>
                <td>${a.triggeredThreshold}</td>
                <td>${a.modeAtTime}</td>
                <td>${a.triggeredAt}</td>
            </tr>
        `;
    });
}

// =====================================================
// SORTING
// =====================================================
function sortBy(column) {

    if (
        CURRENT_SORT_COLUMN === column
    ) {

        CURRENT_SORT_DIRECTION =
            CURRENT_SORT_DIRECTION === "asc"
                ? "desc"
                : "asc";

    } else {

        CURRENT_SORT_COLUMN = column;

        CURRENT_SORT_DIRECTION = "asc";
    }

    loadReadings();
}

// =====================================================
// EXPORT
// =====================================================
function exportData() {

    window.open(
        `${API_REPORT}/export/${CLASSROOM_ID}`
    );
}

// =====================================================
// EXPORT + CLEAN
// =====================================================
function exportAndClean() {

    window.open(
        `${API_REPORT}/export-and-clean/${CLASSROOM_ID}`
    );
}

// =====================================================
// SORT HELPERS
// =====================================================
function getArrow(column) {

    if (
        CURRENT_SORT_COLUMN !== column
    ) return "";

    return CURRENT_SORT_DIRECTION === "asc"
        ? "▲"
        : "▼";
}

function getSortClass(column) {

    return CURRENT_SORT_COLUMN === column
        ? "active-sort"
        : "";
}