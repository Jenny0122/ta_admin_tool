/**
 * 현재 사용자의 활동 기록을 page 단위로 가져온다.
 * @param size
 * @param page
 */
const getPagedBatchHistory = function getPagedHistoryOfCurrentUser(size = 5, page = 1, div = 'B') {
	
	var param = new Object();
	
	param.pageSize = size;
	param.pageRow = page;
	param.logDiv = div;
	
    $.ajax({
        type: "POST",
        data: JSON.stringify(param),
        url: contextPath + '/historyRest/getActionHistoryList/',
        contentType: 'application/json;charset=UTF-8',
        success: function (pagedHistory) {
            const now = Date.now();

            // empty tbody
            const tbody = document.querySelector("table.batchlog tbody");
            tbody.innerHTML = pagedHistory.dataTable;
        },
        error: function (jqXHR) {
            console.error(`Failed to get TMHistory. ${jqXHR.status}. ${jqXHR.responseText}`);
        }
    });

};

/**
 * 현재 사용자의 활동 기록을 page 단위로 가져온다.
 * @param size
 * @param page
 */
const getPagedUserHistory = function getPagedHistoryOfCurrentUser(size = 5, page = 1, div = 'U') {
	
	var param = new Object();
	
	param.pageSize = size;
	param.pageRow = page;
	param.logDiv = div;
	
    $.ajax({
        type: "POST",
        data: JSON.stringify(param),
        url: contextPath + '/historyRest/getActionHistoryList/',
        contentType: 'application/json;charset=UTF-8',
        success: function (pagedHistory) {
            const now = Date.now();

            // empty tbody
            const tbody = document.querySelector("table.userlog tbody");
            tbody.innerHTML = pagedHistory.dataTable;
        },
        error: function (jqXHR) {
            console.error(`Failed to get TMHistory. ${jqXHR.status}. ${jqXHR.responseText}`);
        }
    });

};

function fnDoList(pageRow) {
	
	var param = new Object();
	
	param.pageSize = 10;
	param.pageRow = pageRow;
	param.logDiv = $('#historyFlag').val();
	
    $.ajax({
        type: "POST",
        data: JSON.stringify(param),
        url: contextPath + '/historyRest/getActionHistoryList/',
        contentType: 'application/json;charset=UTF-8',
        success: function (pagedHistory) {

            let resultBody = document.getElementById("resultbody");
            resultBody.innerHTML = pagedHistory.dataTable;
            
            let pageNav = document.getElementById("resultPagination");
            pageNav.innerHTML = pagedHistory.pageNav;
			
        },
        error: function (jqXHR) {
            console.error(`Failed to get TMHistory. ${jqXHR.status}. ${jqXHR.responseText}`);
        }
    });

};

/**
 * 현재 사용자의 TMResource 사용 현황을 가져온다.
 * @constructor
 * @param {Object} mongodbStats
 * @param {Number} mongodbStats.countCollections
 * @param {Number} mongodbStats.countDocuments
 * @param {Number} mongodbStats.countDictionaries
 */
 /*
const getMongoStats = function getMongoStatusOfCurrentUserTMResource() {
    $.ajax({
        type: "GET",
        url: `/dashboard/mongoStats`,
        success: function (mongodbStats) {
            console.debug(mongodbStats);

            let formattingCountDoc = mongodbStats.countDocuments;
            try {
                formattingCountDoc = mongodbStats.countDocuments.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
            } catch (e) {
                console.error("Failed to formatting. ", e);
            }

            document.getElementById("count-collections").innerText = `${mongodbStats.countCollections} 개`;
            document.getElementById("count-documents").innerText = `${formattingCountDoc} 건`;
            document.getElementById("count-dictionaries").innerText = `${mongodbStats.countDictionaries} 건`;
        },
        error: function (jqXHR) {
            console.error("Failed to get status of TMResource");
            console.debug(jqXHR);
        }
    });
};
*/

//  ===================================================================================================
//  Project view functions
//  ===================================================================================================

const getProjectStatus = function(){
    $.ajax({
        type: "GET",
        url: `${contextPath}/dashboard/projectStatus`,
        statusCode:{
            200: function(response){
                console.debug(response);
                
                $("#span-num-project").text(response.projectCnt + " 개");
                $("#count-services").text(response.serviceCnt + " 개");
                $("#count-collections").text(response.collectionCnt + " 개");
                $("#count-dictionaries").text(response.dictionaryCnt + " 개");
            },
            404: function(response){
                console.error("TM server response:", response);
                $("#span-num-project").text("0 개");
                $("#count-services").text("0 개");
                $("#count-collections").text("0 개");
                $("#count-dictionaries").text("0 개");
                
            },
            500:  function(response){
                console.error("TM server response:", response);
                $("#span-num-project").text("0 개");
                $("#count-services").text("0 개");
                $("#count-collections").text("0 개");
                $("#count-dictionaries").text("0 개");
                
            }
        }
    });
};


//  ===================================================================================================
//  Resource view functions
//  ===================================================================================================

const getResourceStatus = function(){
    $.ajax({
        type: "GET",
        url: `${contextPath}/dashboard/resource`,
        statusCode:{
            200: function(response){
                console.debug("TM server response:", response.resourceList);
                const cpuUsage = response.cpu;
                console.debug("CPU usage", cpuUsage);
                drawUsageChart(
                    "cpu-chart",
                    cpuUsage.usage,
                    100,
                    "#448844",
                    "#aaffaa",
                     cpuUsage.ghz + "Ghz"
                );
                const memoryUsage = response.mem;
                console.debug("Memory usage", memoryUsage);
                let usageInGb = parseInt(Math.round(memoryUsage.used/1024/1024/1024));
                let totalInGb = parseInt(Math.round(memoryUsage.total/1024/1024/1024));
                drawUsageChart(
                    "memory-chart",
                    memoryUsage.used,
                    memoryUsage.total,
                    "#448888",
                    "#aaffff",
                    `${usageInGb}/${totalInGb}`
                );
                const diskUsage = response.disk;
                console.debug("Disk usage", diskUsage);
                usageInGb = parseInt(Math.round(diskUsage.used/1024/1024/1024));
                totalInGb = parseInt(Math.round(diskUsage.total/1024/1024/1024));
                drawUsageChart(
                    "disk-chart",
                    diskUsage.used,
                    diskUsage.total,
                    "#444488",
                    "#aaaaff",
                    `${usageInGb}/${totalInGb}`
                );
            },
            409: function (response) {
                console.log("TM server response:", response);
                alert(`${response.responseText}`);
            },
            503: function(response){
                console.error("TM server response:", response);
                const cpuUsage = response.cpu;
                drawUsageChart("cpu-chart",0,100,"#448844","#aaffaa","" );
                drawUsageChart("memory-chart",0,100,"#448888","#aaffff","");
                drawUsageChart("disk-chart",0,100,"#444488","#aaaaff","");
            }
        }
    });
};

const drawUsageChart = function(chartId, used, total, usedColor, totalColor, text){
    const data = {
		labels: [
			"used",
			"free"
		],
        datasets: [
            {
                label: ["used", "free"],
                data: [used, total - used],
                backgroundColor: [usedColor, totalColor],
                borderWidth: 1
            }
        ]
    };

    new Chart($(`#${chartId}`), {
        type: "doughnut",
        data: data,
        options: {
            responsive: true,
            legend: {
                display: false
            },
            title: {
                display: false,
                text: text
            },
            cutoutPercentage: 80
        }
    });
};
