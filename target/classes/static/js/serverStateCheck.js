function getCpuData() { //cpu

    const cpuData = [];

    let cpuTotal = "";
    let cpUsage = "";
    let cpUsed = "";

    $.ajax({
        type: "GET",
        async: false,
        url: `/dashboard/tm-server/resource/cpu`,
        success: function (data) {
            cpuTotal = data.total;
            cpUsage = data.usage;
            cpUsed = data.used;
            cpuData.push({total: cpuTotal, usage: cpUsage,  used: cpUsed});

        },
        error: function (request, status, error) {
            console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + error);
        }
    });

    var ctx = document.getElementById('myChart1').getContext('2d');
    ctx.canvas.width = 230;
    ctx.canvas.height = 230;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ["CPU"],
            datasets: [
                {
                    label: "2.4Ghz",
                    backgroundColor: ["#009900", "#ccffcc"],
                    data: [cpUsage, "100"],
                }
            ]
        },
        options: {
            title: {
                display: true
            },
            legend: {
                display: true,
            },
            responsive: false, // Instruct chart js to respond nicely.
            maintainAspectRatio: false, // div 크기에 반응
            cutoutPercentage: 70 //두께 조절
        }
    });

    return cpuData;
}
function getMemoryData(){
    const memData = [];
    let memTotal = "";
    let memUsage = "";
    let memUsed = "";
    $.ajax({
        type: "GET",
        async: false,
        url: `/dashboard/tm-server/resource/memory`,
        success: function (data) {
            memTotal = data.total;
            memUsage = data.usage;
            memUsed = data.used;
            memData.push({total: memTotal, usage: memUsage,  used: memUsed});
        },
        error: function (request, status, error) {
            console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + error);
        }
    });

    var ctx = document.getElementById('myChart2').getContext('2d');
    ctx.canvas.width = 230;
    ctx.canvas.height = 230;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ["Memory"],
            datasets: [
                {
                    label: "2.4Ghz",
                    backgroundColor: ["#3cba9f", "#e6ffe6"],
                    data: [memUsed, memTotal], /* 현재 사용량 | 총 사용량 */
                }
            ]
        },
        options: {
            title: {
                display: true
            },
            legend: {
                display: true,
            },
            responsive: false,
            maintainAspectRatio: false, // div 크기에 반응
            cutoutPercentage: 70 //두께 조절
        }
    });
    return memData;
}
function getHDData(){
    const getHDData = [];
    let HDDTotal = "";
    let HDDUsage = "";
    let HDDUsed = "";
    $.ajax({
        type: "GET",
        async: false,
        url: `/dashboard/tm-server/resource/disk`,
        success: function (data) {
            HDDTotal = data.total;
            HDDUsage = data.usage;
            HDDUsed = data.used;
            getHDData.push({total: HDDTotal, usage: HDDUsage,  used: HDDUsed});
        },
        error: function (request, status, error) {
            console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + error);
        }
    });

    var ctx = document.getElementById('myChart3').getContext('2d');
    ctx.canvas.width = 230;
    ctx.canvas.height = 230;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ["HDD"],
            datasets: [
                {
                    label: "2.4Ghz",
                    backgroundColor: ["#3333cc", "#c2c2f0"],
                    data: [HDDUsed, HDDTotal], /* 현재 사용량 | 총 사용량 */

                }
            ]
        },
        options: {
            title: {
                display: true
            },
            legend: {
                display: true,
            },
            responsive: false,
            maintainAspectRatio: false, // div 크기에 반응
            cutoutPercentage: 70 //두께 조절
        }
    });
        return  getHDData;
}