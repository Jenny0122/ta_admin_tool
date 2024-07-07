var idCheckRslt = '';

/**
 * 사용자 정보 채우기
 * @returns {boolean}
 */
const showUpdatePopup = function () {
    // [1] get username
    let userId = $("input[type=radio][name=user_choice]:checked").val();
    if (!userId) {
        alert(`수정하고자 하는 사용자를 먼저 선택해주세요`);
        return false;
    }

    $.ajax({
        async: false,
        type: "GET",
        url: `${contextPath}/user/getUserInfo?userId=` + userId,
        /**
         *
         * @param {Object} user
         * @param {String} user.username
         * @param {String} user.name
         * @param {String} user.email
         * @param {String} user.userType
         * @param {Array} user.authorities
         * @param {Object} user.authorities[]
         * @param {String} user.authorities[].authority
         */
        success: function (user) {
            let $target = $("#update_pop");

            // [2] user info 채우기
            $target.find("input[name=userId]").val(user.userId);
            $target.find("input[name=userName]").val(user.userName);
            $target.find("input[name=userEmail]").val(user.userEmail);
            // $target.find('select[name=authorities]').val(user.authorities[0].authority);

            // [3] 해당 팝업창 띄우기
            $target.show();
        },
        error: function (data) {
            console.error("Failed to get User");
            console.debug(data);
            alert("사용자 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
        }
    });
};

/**
 * 팝업창 띄우기
 * @param target
 */
const showPopup = function (target) {
    let $target;
    switch (target) {
        case "create":
			$('#userid').attr('readonly', false);
			$('#user_name').attr('readonly', false);
			$('#email').attr('readonly', false);
			
            $target = $("#create_pop");
            $target.find("input,select").val("");
            $target.show();
            $target.find("input[type=text]:eq(0)").focus();
            break;
        case "update":
            showUpdatePopup();
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
};

/**
 * 팝업창 숨기기
 * @param target
 */
const hidePopup = function (target) {
    let $target;
    switch (target) {
        case "create":
            $target = $("#create_pop");
            break;
        case "update":
            $target = $("#update_pop");
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
    $target.find("input,select").val("");
    $target.hide();
};

/**
 * 선택된 사용자 삭제
 */
const deleteUser = function deleteSelectedUser() {
    const userId = $("input[type=radio][name=user_choice]:checked").val();
    if (!userId) {
        alert(`삭제하고자 하는 사용자를 먼저 선택해주세요`);
        return false;
    }

    if (confirm(`선택한 사용자(id=${userId})을 삭제합니다. 삭제된 사용자는 복구 불가능 합니다.`)) {
        $.ajax({
            type: "POST",
            url: `${contextPath}/user/deleteUser`,
            data: {"userId" : userId},
            success: function (data) {
				if (data.result == "S") {
	                alert(`사용자(id='${userId}')가 삭제되었습니다.`);
	                location.reload();
				} else {
					alert(resultMsg);					
				}
            },
            error: function (data) {
                console.error("Failed to delete User" + data);
                alert("사용자 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
            }
        });
    }
};

/**
 * 사용자 생성
 * @returns {boolean}
 */
const createUser = function () {
    let $form_user = $("#create_pop form");

    if (!isFormValid(document.querySelector("#create_pop form"))) {
        return false;
    }

	if (idCheckRslt != 'Y') {
		alert("SSO 연동을 클릭해주세요.");
        return false;
	}

    $.ajax({
        type: "POST",
        url: `${contextPath}/user/register`,
        data: $form_user.serialize(), //default contentType: "application/x-www-form-urlencoded"
        success: function (data) {
            alert(data.resultMsg);

			if (data.result == "S"){
	            location.reload();
			}
        },
        error: function (data) {
            console.error("Failed to create User", data);
            alert("사용자 생성 실패. 서버 연결 상태 및 로그를 확인하세요.");
        }
    });

    return false;
};

/**
 * 사용자 수정
 * @returns {boolean}
 */
const updateUser = function () {
    let $form_user = $("#update_pop form");

    if (!isFormValid(document.querySelector("#update_pop form"))) {
        return false;
    }

    const userId = document.getElementById("useridUP").value;

	console.log($form_user.serialize());

    $.ajax({
        type: "POST",
        url: `${contextPath}/user/updateUser`,
        data: $form_user.serialize(), //default contentType: "application/x-www-form-urlencoded"
        beforeSend: function () {
        },
        success: function (data) {
            alert(data.resultMsg);

			if (data.result == "S"){
	            location.reload();
			}
        },
        error: function (data) {
            console.error("Failed to update User");
            console.debug(data);
            alert("사용자 수정 실패. 서버 연결 상태 및 로그를 확인하세요.");
        }
    });

    return false;
};

const idCheck = function () {
	var userId = $('#userid').val();
	
	$.ajax({
        type: "GET",
        url: `${contextPath}/user/checkUser/${userId}`,
        beforeSend: function () {
			idCheckRslt = '';
        },
        success: function (response) {
			if (response.result == "S"){
	            $('#userid').attr('readonly', true);
	            $('#user_name').val(response.userName).attr('readonly', true);
	            response.userEmail != null ? $('#email').val(response.userEmail).attr('readonly', true) : $('#email').val('-');
	            $('#hlfcDscd').val(response.hlfcDscd);	// 재직구분코드
	            $('#blntBrno').val(response.blntBrno);	// 소속점번호
	            idCheckRslt = 'Y';
			} else {
				alert(response.resultMsg);				
			}
        },
        error: function (data) {
            console.error("Failed to update User");
            console.debug(data);
            alert("SSO 연동 실패. 서버 연결 상태 및 로그를 확인하세요.");
        }
    });
}

const isFormValid = function (formElement) {
    let userId = formElement.querySelector("input[name=userId]");
    if (!userId.value) {
        alert("아이디를 입력해주세요");
        userId.focus();
        return false;
    }

	// ID는 사번으로 사용될 예정으로 체크 안함
	/*
    const regexId = /^[0-9a-zA-Z]{4,24}$/;
    if (!regexId.test(userId.value)) {
        alert("아이디는 4~24자 영문 대 소문자, 숫자를 사용하세요.");
        userId.focus();
        return false;
    }
	*/
	
    let userName = formElement.querySelector("input[name=userName]");
    if (!userName.value) {
        alert("이름은 필수 정보입니다.");
        userName.focus();
        return false;
    }

    let userAuth = formElement.querySelector("select[name=userAuth]");
    if (!userAuth.value) {
        alert("권한을 선택해주세요.");
        userAuth.focus();
        return false;
    }

/*
    const userEmail = formElement.querySelector("input[name=userEmail]");
    if (!userEmail.value) {
        alert("이메일은 필수 정보입니다.");
        userEmail.focus();
        return false;
    }


    const regexPseudoEmail = /^\S+@\S+\.\S+$/;
    if (!regexPseudoEmail.test(userEmail.value)) {
        alert("이메일이 올바르지 않습니다.");
        userEmail.focus();
        return false;
    }
*/

    let userPw = formElement.querySelector("input[name=userPw]");
    if (userPw.value) {
        const regexPw = /^((?=.*[a-zA-Z0-9])(?=.*[^a-zA-Z0-9]).{8,16})$/;
        if (!regexPw.test(userPw.value)) {
            alert("비밀번호는 특수문자를 최소 하나 포함한 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.");
            userPw.focus();
            return false;
        }

        let passwordConfirm = formElement.querySelector("input[name=passwordConfirm]");
        if (passwordConfirm && (userPw.value != passwordConfirm.value)) {
            alert("비밀번호가 일치하지 않습니다.");
            passwordConfirm.focus();
            return false;
        }
    }

    return true;
};

/**
 * 비활성화 상태의 사용자 재 활성화 요청
 * @returns {boolean}
 */
const enableUser = function forceEnableUser() {
    const userId = document.querySelector("input[type=radio][name=user_choice]:checked").value;
    if (!userId) {
        alert("활성화하고자 하는 사용자를 먼저 선택해주세요");
        return false;
    }

	var params = { 
				   "userId" : userId
        	  	 , "useYn" : "Y"
        	  	 };

    $.ajax({
        type: "POST",
        url: `${contextPath}/user/updateUserEnabled`,
        data: JSON.stringify(params), //default contentType: "application/x-www-form-urlencoded"
		contentType: "application/json; charset=UTF-8",
        success: function (data) {
			if (data.result == "S") {
	            alert(`사용자(id='${userId}')가 활성화 되었습니다.`);
	            location.reload();
            } else {
				alert(data.resultMsg);
			}
        },
        error: function (jqXHR) {
            console.error("Failed to update User. " + jqXHR);
            alert("사용자 활성화 실패. 서버 연결 상태 및 로그를 확인하세요.");
        }
    });

    return false;
};

/**
 * 사용자 비활성화 요청
 * @returns {boolean}
 */
const disableUser = function forceDisableUser() {
    const userId = document.querySelector("input[type=radio][name=user_choice]:checked").value;

    if (!userId) {
        alert("비활성화하고자 하는 사용자를 먼저 선택해주세요");
        return false;
    }

    if (!confirm(`사용자(id='${userId}')를 비활성화합니다.\n이후 접속을 차단하고 사용자 소유의 자원은 사용할 수 없게됩니다.`)) {
        return false;
    }

	var params = { 
				   "userId" : userId
        	  	 , "useYn" : "N"
        	  	 }
        	  	 
    $.ajax({
        type: "POST",
        url: `${contextPath}/user/updateUserEnabled`,
        data: JSON.stringify(params), //default contentType: "application/x-www-form-urlencoded"
		contentType: "application/json; charset=UTF-8",
        success: function (data) {
			if (data.result == "S") {
	            alert(`사용자(id="${userId}')가 비활성화 되었습니다.`);
	            location.reload();
            } else {
				alert(data.resultMsg);
			}
        },
        error: function (jqXHR) {
            console.error("Failed to update User. " + jqXHR);
            alert(`사용자 비활성화 실패했습니다.\n${jqXHR.responseText}`);
        }
    });

    return false;
};

