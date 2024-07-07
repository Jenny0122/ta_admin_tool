// Context path
const contextPath = $('#contextPathHolder').attr('data-contextPath') ? $('#contextPathHolder').attr('data-contextPath') : '';

// table row 클릭시 해당 row 하위의 input[type=radio] 클릭
const radioByTableRowClick = function radioSelectByClickParentTableRow(selector = "tr") {
    $(selector).on("click", function (event) {
        if (event.target.type !== "i") {
            try {
                $(this).find("td input[type=radio]")[0].click();
            } catch (e) {

            }
        }
    });
};

// table row 클릭시 해당 row 하위의 input[type=checkbox] 클릭
const checkByTableRowClick = function checkboxCheckByClickParentTableRow(selector = "tr") {
    $(selector).on("click", function (event) {
        if (event.target.type !== "checkbox") {
            try {
                $(this).find("td input[type=checkbox]")[0].click();
            } catch (e) {

            }
        }
    });
};

// craete, update, delete 등의 popup 시 음영처리된 백그라운드 화면을 클릭해도 닫힐 수 있도록 클릭이벤트 리스너 추가
const closePopupByBgDblClick = function closePopupDivByDoubleClickChildOverayBackground() {
    $(".marks_overlay div.bg").on("dblclick", function (event) {
        const popupCloseIcon = event.target.parentElement.querySelector("i.fa-times");
        if (popupCloseIcon) {
            popupCloseIcon.click();
        }
    });
};

/**
 * 페이지네이션된 url 에서 페이지 이동
 * @param page 이동할 페이지. start with 0
 * @param size 페이지당 보여줄 데이터 수. default 10
 * @param searchPattern 검색결과 페이지의 경우 검색쿼리.
 */
const movePage = function changeCurrentLocationByPagingInfo(page = 0, size = 10, sort = '', searchPattern = '') {
    if (!searchPattern && searchPattern === "") {
        searchPattern = (new URL(window.location)).searchParams.get("searchPattern");
    }

    const urlSearchParams = new URLSearchParams();
    urlSearchParams.append("page", page);
    urlSearchParams.append("size", size);

    if (sort && sort.indexOf(":") > 1) {
        let [field, order] = sort.split(/\s?:\s?/);
        order = order.toUpperCase().startsWith("DESC") ? "DESC" : "ASC";
        if (field && order) {
            urlSearchParams.append("sort", `${field},${order}`);
        }
    }

    // for dictionary entry search
    if (searchPattern) {
        urlSearchParams.append("searchPattern", searchPattern);
    }

    window.location.href = `${window.location.pathname}?${urlSearchParams.toString()}`;
};

/**
 * tm-server 연결 테스트. 실패시 Alert 창을 띄우고 mypage 로 이동시킨다.
 */
const testConnectionTmServer = function testConnectionTextMinerServerByProperties(alertSuccess = false) {
    $.ajax({
        type: "GET",
        url: `/test/tm-server`,
        /**
         * @param {Object} mongodbStats
         * @param {Number} mongodbStats.countCollections
         * @param {Number} mongodbStats.countDocuments
         * @param {Number} mongodbStats.countDictionaries
         */
        success: function (response) {
            console.debug(response);
            if (alertSuccess) {
                alert("TM Server 연결 성공");
            }
        },
        error: function (jqXHR) {
            console.error("Failed to connect tm-server");

            if (jqXHR.status === 503) {
                if (confirm("TM Server 연결을 확인해주세요.\n" +
                    "문제가 계속 될 경우 관리자에게 문의 바랍니다.")) {
                    location.href = "/user/mypage";
                }
            }
        }
    });
};

/**
 * MongoDB 연결 테스트. 실패시 alert 창을 띄우고 계속해서 체크한다.
 * @param pooling 반복여부
 * @param alertSuccess 성공표시여부
 */
const testConnectionMongoDB = function testConnectionMongoDatabaseByProperties(pooling = false, alertSuccess = false) {
    $.ajax({
        type: "GET",
        url: "/test/mongo-db",
        timeout: 2000,
        success: function (response) {
            consoel.debug(response);
            if (alertSuccess) {
                alert("MongoDB 연결 성공");
            }
        },
        error: function (response) {
            alert("데이터베이스 연결을 확인해주세요.\n" +
                "문제가 계속 될 경우 관리자에게 문의 바랍니다.");

            // db 연결 실패일 경우 관리도구 사용 불가. 계속 체크
            if (pooling) {
                setTimeout(testConnectionMongoDB, 5000);
            }
        }
    });
};

/**
 * bytes --> KB, MB, ....
 * @param {Number} x
 * @returns {string|*}
 */
const get_file_size = function bytesFileSizeRepresentByte(x) {
    if (x === 0 || x === "0") {
        return "0 B";
    }
    if (Number.isNaN(x)) {
        return x;
    }
    const s = ["B", "KB", "MB", "GB", "TB", "PB"];
    let e = Math.floor(Math.log(x) / Math.log(1024));
    if (e === 0) { // byte --> no fixed-point
        return `${(x / Math.pow(1024, e))} ${s[e]}`;
    }
    return `${(x / Math.pow(1024, e)).toFixed(1)} ${s[e]}`;
};

/**
 * input field 의 앞뒤 공백 제거
 */
const trimInputFields = function () {
    $(":input").each(function () {
        $(this).val($.trim($(this).val()));
    });
};

const ServerConnectionStatus = (function () {
    // private
    let tmConnection = true;
    let managerConnection = true;

    return {
        isTMConnected() {
            return tmConnection;
        },
        connectTM() {
            tmConnection = true;
        },
        disconnectTM() {
            tmConnection = false;
        },
        isManagerConnected() {
            return managerConnection;
        },
        connectManager() {
            managerConnection = true;
        },
        disconnectManager() {
            managerConnection = false;
        }
    };
}());

$(document).ajaxError(function (event, jqxhr, settings, thrownError) {
    console.warn("request: ", settings.type, settings.url, "\nresponse: ", jqxhr.status, jqxhr.responseJSON);
    if (jqxhr.status === 401) {
        if (ServerConnectionStatus.isManagerConnected()) {
            ServerConnectionStatus.disconnectManager();

            console.warn("session invalidated.");
            alert("로그아웃 되었습니다.");
            location.reload();
        }
    } else if (jqxhr.status === 503) {
        if (ServerConnectionStatus.isTMConnected()) {
            ServerConnectionStatus.disconnectTM();

            if (!location.pathname.startsWith("/user/mypage")) {
                console.warn("TM Server disconnected. redirect to '/user/mypage'");
                alert("TM Server 연결에 실패했습니다. 관리자에게 문의해주세요.");
                // location.href = "/user/mypage";
	            location.reload();
            }
        }
    }
});

window.onerror = function (message, file, line, col, error) {
    // You can send data to your server
    // sendError(data);
    /*if (error instanceof TMServerConnectionFailed) {
        console.debug(error);
        alert("TM Server 연결을 확인해주세요.");
        window.location = "/user/mypage";
    }*/

    console.debug(message, "from", error.stack);
};

// Sample File Download for Upload Collection or Dictionary Data
function sampleDownload (fileType, fileExt) {
	
	var filePath = '../sample/' + fileType +'/sample_' + fileType + '.' + fileExt;
	var a = document.createElement('a');
	a.setAttribute('type', 'application/octet-stream;charset=utf-8');
	a.setAttribute('href', filePath);
	a.setAttribute('download', 'sample_' + fileType + '.' + fileExt);
	a.click();
	a.remove();
	
}

function downloadManual() {
	var filePath = '../manual/TM_USER_MANUAL';
	var a = document.createElement('a');
	a.setAttribute('type', 'application/octet-stream;charset=utf-8');
	a.setAttribute('href', filePath);
	a.setAttribute('download', 'TA_관리도구_메뉴얼.pdf');
	a.click();
	a.remove();
}