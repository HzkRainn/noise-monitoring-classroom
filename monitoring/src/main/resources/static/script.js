const API_BASE = "http://localhost:8080/api/noise";

let chart;

// ======================================
// REFRESH INDICATOR
// ======================================
function showRefresh() {

    const indicator =
        document.getElementById("refreshIndicator");

    indicator.innerText = "🔄 Refreshing...";

    setTimeout(() => {
        indicator.innerText = "✅ Updated";
    }, 1000);
}

// ======================================
// LOAD DASHBOARD
// ======================================
async function loadDashboard() {

    showRefresh();

    // ======================================
    // GET LATEST READINGS
    // ======================================
    const readingsRes = await fetch(
        `${API_BASE}/latest/1?limit=100`
    );

    const readings = await readingsRes.json();

    // Urutkan agar chart chronological
    readings.reverse();

    // ======================================
    // TOTAL READINGS
    // ======================================
    const countRes = await fetch(
        `${API_BASE}/count/1`
    );

    const totalCount = await countRes.json();

    document.getElementById(
        "totalReadings"
    ).innerText = totalCount;

    // ======================================
    // GET CURRENT MODE
    // ======================================
    const modeRes = await fetch(
        `${API_BASE}/learn/1`
    );

    const modeData = await modeRes.json();

    const modeElement =
        document.getElementById("mode");

    let thresholdValue = 0;
    let modeName = "-";

    if (modeData) {

        modeName = modeData.mode;

        thresholdValue =
            modeData.thresholdValue || 0;

        document.getElementById(
            "threshold"
        ).innerText =
            modeData.thresholdValue != null
                ? modeData.thresholdValue.toFixed(2)
                : "-";

        // ======================================
        // MODE COLOR
        // ======================================
        let bgColor = "#007bff";

        switch (modeData.mode) {

            case "FOCUSED":
                bgColor = "green";
                break;

            case "DISCUSSION":
                bgColor = "blue";
                break;

            case "CHAOTIC":
                bgColor = "red";
                break;

            case "HUMAN_ACTIVITY":
                bgColor = "orange";
                break;

            case "MACHINE_NOISE":
                bgColor = "purple";
                break;
        }

        modeElement.style.backgroundColor =
            bgColor;

    } else {

        document.getElementById(
            "threshold"
        ).innerText = "-";

        modeElement.style.backgroundColor =
            "#999";
    }

    modeElement.innerText = modeName;

    // ======================================
    // GET LATEST ALARMS
    // ======================================
    const alarmRes = await fetch(
        "http://localhost:8080/api/report/latest-alarms/1"
    );

    const alarms = await alarmRes.json();

    // ======================================
    // TOTAL ALARMS
    // ======================================
    const alarmCountRes = await fetch(
        "http://localhost:8080/api/alarm/count/1"
    );

    const totalAlarms =
        await alarmCountRes.json();

    document.getElementById(
        "totalAlarms"
    ).innerText = totalAlarms;

    // ======================================
    // RENDER ALARM TABLE
    // ======================================
    const tbody =
        document.querySelector(
            "#alarmTable tbody"
        );

    tbody.innerHTML = "";

    alarms.forEach(alarm => {

        const row = `
            <tr>
                <td>${alarm.id}</td>
                <td>${alarm.classroomId}</td>
                <td>${alarm.actualDb}</td>
                <td>${alarm.triggeredThreshold}</td>
                <td>${alarm.modeAtTime}</td>
                <td>${alarm.triggeredAt}</td>
            </tr>
        `;

        tbody.innerHTML += row;
    });

    // ======================================
    // RENDER CHART
    // ======================================
    renderChart(
        readings,
        thresholdValue,
        modeName
    );
}

// ======================================
// CHART RENDER
// ======================================
function renderChart(
    readings,
    threshold,
    mode
) {

    if (chart) {
        chart.destroy();
    }

    const labels =
        readings.map(r => r.id);

    const data =
        readings.map(r => r.dbLevel);

    const thresholdLine =
        readings.map(() => threshold);

    // ======================================
    // MODE COLOR
    // ======================================
    let lineColor = "#007bff";

    switch (mode) {

        case "FOCUSED":
            lineColor = "green";
            break;

        case "DISCUSSION":
            lineColor = "blue";
            break;

        case "CHAOTIC":
            lineColor = "red";
            break;

        case "HUMAN_ACTIVITY":
            lineColor = "orange";
            break;

        case "MACHINE_NOISE":
            lineColor = "purple";
            break;
    }

    const ctx =
        document.getElementById(
            "noiseChart"
        );

    chart = new Chart(ctx, {

        type: "line",

        data: {

            labels: labels,

            datasets: [

                {
                    label: "Noise Level (dB)",

                    data: data,

                    borderWidth: 2,

                    borderColor: lineColor,

                    tension: 0.3
                },

                {
                    label: "Adaptive Threshold",

                    data: thresholdLine,

                    borderWidth: 2,

                    borderDash: [5, 5],

                    borderColor: "black",

                    tension: 0
                }
            ]
        },

        options: {

            responsive: true,

            plugins: {
                legend: {
                    display: true
                }
            },

            scales: {

                x: {
                    title: {
                        display: true,
                        text:
                        "Sensor Reading ID (Chronological)"
                    }
                },

                y: {
                    title: {
                        display: true,
                        text:
                        "Noise Level (dB)"
                    }
                }
            }
        }
    });
}

// ======================================
// INITIAL LOAD
// ======================================
loadDashboard();

// ======================================
// AUTO REFRESH
// ======================================
setInterval(() => {
    loadDashboard();
}, 5000);