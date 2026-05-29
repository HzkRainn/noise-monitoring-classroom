const API_BASE =
"http://localhost:8080/api/noise";

const API_REPORT =
"http://localhost:8080/api/report";

const API_ALARM =
"http://localhost:8080/api/alarm";

const REFRESH_INTERVAL =
5000;

let chart;

let CLASSROOM_ID = 1;

// ======================================
// INIT
// ======================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        initializeDashboard();

        startRealtimeClock();

        loadDashboard();

        setInterval(() => {

            loadDashboard();

        }, REFRESH_INTERVAL);
    }
);

// ======================================
// INITIALIZE
// ======================================

function initializeDashboard() {

    const classroomSelect =
        document.getElementById(
            "classroomSelect"
        );

    if (classroomSelect) {

        classroomSelect.addEventListener(
            "change",
            function () {

                CLASSROOM_ID =
                    parseInt(this.value);

                loadDashboard();
            }
        );
    }
}

// ======================================
// LIVE CLOCK
// ======================================

function startRealtimeClock() {

    setInterval(() => {

        const now =
            new Date();

        const clock =
            document.getElementById(
                "liveClock"
            );

        if (clock) {

            clock.innerText =
                now.toLocaleTimeString();
        }

    }, 1000);
}

// ======================================
// REFRESH STATUS
// ======================================

function updateRefreshStatus() {

    const indicator =
        document.getElementById(
            "refreshIndicator"
        );

    if (indicator) {

        indicator.innerText =
            "Synced "
            + new Date().toLocaleTimeString();
    }
}

// ======================================
// LOAD DASHBOARD
// ======================================

async function loadDashboard() {

    try {

        updateRefreshStatus();

        // ======================================
        // FETCH API
        // ======================================

        const [
            readingsRes,
            countRes,
            modeRes,
            alarmRes,
            totalAlarmRes
        ] = await Promise.all([

            fetch(
                `${API_BASE}/latest/${CLASSROOM_ID}?limit=100`
            ),

            fetch(
                `${API_BASE}/count/${CLASSROOM_ID}`
            ),

            fetch(
                `${API_BASE}/latest-mode/${CLASSROOM_ID}`
            ),

            fetch(
                `${API_BASE}/latest-alarms/${CLASSROOM_ID}`
            ),

            fetch(
                `${API_ALARM}/count/${CLASSROOM_ID}`
            )
        ]);

        // ======================================
        // JSON PARSE
        // ======================================

        let readings = [];
        let totalCount = 0;
        let modeData = null;
        let alarms = [];
        let totalAlarms = 0;

        try {

            readings =
                await readingsRes.json();

        } catch {

            readings = [];
        }

        try {

            totalCount =
                await countRes.json();

        } catch {

            totalCount = 0;
        }

        try {

            modeData =
                await modeRes.json();

        } catch {

            modeData = null;
        }

        try {

            alarms =
                await alarmRes.json();

        } catch {

            alarms = [];
        }

        try {

            totalAlarms =
                await totalAlarmRes.json();

        } catch {

            totalAlarms = 0;
        }

        // ======================================
        // SORT ASCENDING
        // ======================================

        readings.reverse();

        // ======================================
        // TOTAL READINGS
        // ======================================

        const totalReadingsElement =
            document.getElementById(
                "totalReadings"
            );

        if (totalReadingsElement) {

            totalReadingsElement.innerText =
                totalCount;
        }

        // ======================================
        // TOTAL ALARMS
        // ======================================

        const totalAlarmElement =
            document.getElementById(
                "totalAlarms"
            );

        if (totalAlarmElement) {

            totalAlarmElement.innerText =
                totalAlarms;
        }

        // ======================================
        // MODE + THRESHOLD
        // ======================================

        const modeElement =
            document.getElementById(
                "mode"
            );

        const thresholdElement =
            document.getElementById(
                "threshold"
            );

        let thresholdValue = 0;

        let modeName = "DEFAULT";

        if (modeData) {

            modeName =
                modeData.mode || "DEFAULT";

            thresholdValue =
                modeData.thresholdValue || 0;

            if (thresholdElement) {

                thresholdElement.innerText =
                    thresholdValue
                    .toFixed(2)
                    + " dB";
            }

            if (modeElement) {

                modeElement.innerText =
                    modeName;

                applyModeColor(
                    modeElement,
                    modeName
                );
            }

        }

        else {

            if (modeElement) {

                modeElement.innerText =
                    "DEFAULT";
            }

            if (thresholdElement) {

                thresholdElement.innerText =
                    "-";
            }
        }

        // ======================================
        // CURRENT DB
        // ======================================

        const currentDbElement =
            document.getElementById(
                "currentDb"
            );

        if (
            currentDbElement
            &&
            readings.length > 0
        ) {

            const latestReading =
                readings[
                    readings.length - 1
                ];

            currentDbElement.innerText =
                latestReading.dbLevel
                ?.toFixed(2)
                + " dB";
        }

        // ======================================
        // AI PREDICTION
        // ======================================

        const predictionElement =
            document.getElementById(
                "prediction"
            );

        if (
            predictionElement
            &&
            readings.length > 0
        ) {

            const latestReading =
                readings[
                    readings.length - 1
                ];

            const prediction =
                latestReading.mlPrediction
                ||
                latestReading.trainingLabel
                ||
                "UNKNOWN";

            predictionElement.innerText =
                prediction;

            applyPredictionColor(
                predictionElement,
                prediction
            );
        }

        // ======================================
        // TABLE
        // ======================================

        renderAlarmTable(
            alarms
        );

        // ======================================
        // CHART
        // ======================================

        renderChart(
            readings,
            thresholdValue,
            modeName
        );

    }

    catch (error) {

        console.error(
            "DASHBOARD ERROR : ",
            error
        );
    }
}

// ======================================
// MODE COLOR
// ======================================

function applyModeColor(
    element,
    mode
) {

    let background =
        "linear-gradient(135deg,#3b82f6,#2563eb)";

    switch (mode) {

        case "FOCUSED":

            background =
                "linear-gradient(135deg,#00c853,#00ff84)";

            break;

        case "DISCUSSION":

            background =
                "linear-gradient(135deg,#00b7ff,#005eff)";

            break;

        case "CHAOTIC":

            background =
                "linear-gradient(135deg,#ff4d6d,#ff1744)";

            break;

        case "HUMAN_ACTIVITY":

            background =
                "linear-gradient(135deg,#ff9800,#ff5722)";

            break;

        case "MACHINE_NOISE":

            background =
                "linear-gradient(135deg,#9c27b0,#673ab7)";

            break;

        case "EXAM_MODE":

            background =
                "linear-gradient(135deg,#00ffd5,#00b7ff)";

            break;
    }

    element.style.background =
        background;
}

// ======================================
// PREDICTION COLOR
// ======================================

function applyPredictionColor(
    element,
    prediction
) {

    if (!element) {

        return;
    }

    if (prediction === "HUMAN") {

        element.style.color =
            "#00ff84";
    }

    else if (
        prediction === "NON_HUMAN"
    ) {

        element.style.color =
            "#ff4d6d";
    }

    else {

        element.style.color =
            "#ffffff";
    }
}

// ======================================
// RENDER ALARM TABLE
// ======================================

function renderAlarmTable(
    alarms
) {

    const tbody =
        document.querySelector(
            "#alarmTable tbody"
        );

    if (!tbody) {

        return;
    }

    tbody.innerHTML = "";

    if (
        !alarms
        ||
        alarms.length === 0
    ) {

        tbody.innerHTML = `

            <tr>

                <td colspan="6"
                    style="
                        text-align:center;
                        padding:30px;
                        color:#94a3b8;
                    "
                >

                    No alarm activity

                </td>

            </tr>
        `;

        return;
    }

    alarms.forEach(alarm => {

        tbody.innerHTML += `

            <tr>

                <td>${alarm.id ?? "-"}</td>

                <td>${alarm.classroomId ?? "-"}</td>

                <td>
                    ${alarm.actualDb
                        ? alarm.actualDb.toFixed(2)
                        : "-"}
                </td>

                <td>
                    ${alarm.triggeredThreshold
                        ? alarm.triggeredThreshold.toFixed(2)
                        : "-"}
                </td>

                <td>
                    ${alarm.modeAtTime ?? "-"}
                </td>

                <td>
                    ${formatDate(
                        alarm.triggeredAt
                    )}
                </td>

            </tr>
        `;
    });
}

// ======================================
// FORMAT DATE
// ======================================

function formatDate(
    dateString
) {

    if (!dateString) {

        return "-";
    }

    const date =
        new Date(dateString);

    return date.toLocaleString();
}

// ======================================
// RENDER CHART
// ======================================

function renderChart(
    readings,
    threshold,
    mode
) {

    const canvas =
        document.getElementById(
            "noiseChart"
        );

    if (!canvas) {

        return;
    }

    if (chart) {

        chart.destroy();
    }

    const labels =
        readings.map(
            r => r.id
        );

    const data =
        readings.map(
            r => r.dbLevel
        );

    const thresholdLine =
        readings.map(
            () => threshold
        );

    let lineColor =
        "#00b7ff";

    switch (mode) {

        case "FOCUSED":

            lineColor =
                "#00ff84";

            break;

        case "DISCUSSION":

            lineColor =
                "#00b7ff";

            break;

        case "CHAOTIC":

            lineColor =
                "#ff4d6d";

            break;

        case "HUMAN_ACTIVITY":

            lineColor =
                "#ff9800";

            break;

        case "MACHINE_NOISE":

            lineColor =
                "#9c27b0";

            break;

        case "EXAM_MODE":

            lineColor =
                "#00ffd5";

            break;
    }

    chart = new Chart(canvas, {

        type: "line",

        data: {

            labels: labels,

            datasets: [

                {

                    label:
                    "Noise Level (dB)",

                    data: data,

                    borderWidth: 3,

                    borderColor:
                    lineColor,

                    backgroundColor:
                    lineColor,

                    pointRadius: 4,

                    pointHoverRadius: 6,

                    tension: 0.35
                },

                {

                    label:
                    "Adaptive Threshold",

                    data: thresholdLine,

                    borderWidth: 3,

                    borderDash: [8, 6],

                    borderColor:
                    "#ffffff",

                    pointRadius: 0,

                    tension: 0
                }
            ]
        },

        options: {

            responsive: true,

            maintainAspectRatio: false,

            interaction: {

                mode: "index",

                intersect: false
            },

            plugins: {

                legend: {

                    labels: {

                        color: "#ffffff",

                        font: {

                            size: 14,

                            family: "Poppins"
                        }
                    }
                }
            },

            scales: {

                x: {

                    ticks: {

                        color: "#cbd5e1"
                    },

                    grid: {

                        color:
                        "rgba(255,255,255,0.06)"
                    }
                },

                y: {

                    beginAtZero: true,

                    ticks: {

                        color: "#cbd5e1"
                    },

                    grid: {

                        color:
                        "rgba(255,255,255,0.06)"
                    }
                }
            }
        }
    });
}