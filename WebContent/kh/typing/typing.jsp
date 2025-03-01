<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script src="https://code.jquery.com/jquery-3.6.0.js"></script>

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/css/bootstrap.min.css"
	rel="stylesheet">
<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/js/bootstrap.bundle.min.js"></script>
<link rel="stylesheet"
	href="https://pro.fontawesome.com/releases/v5.10.0/css/all.css"
	integrity="sha384-AYmEC3Yw5cVb3ZcuHtOA93w35dYTsvhLPVnYs9eStHfGJvOvKxVfELGroGkvsg+p"
	crossorigin="anonymous" />

<style>
* {
	box-sizing: border-box;
	text-align: center;
}

body {
	background-color: #D8E3E7;
}

.container {
	margin-top: 80px;
}

.row {
	text-align: center;
	margin-bottom: 4%;
}

.chart {
	background-color: white;
	border: 2px solid grey;
	box-shadow: 3px 3px 3px grey;
	margin-left: 15%;
	margin-right: 15%;
}

.bar {
	display: inline-block;
	position: relative;
	background-color: cornflowerblue;
}

input {
	height: 100%;
	width: 100%;
}

.record {
	padding: 5px;
	border: 2px solid grey;
	margin-left: 15%;
	margin-right: 15%;
	box-shadow: 3px 3px 3px grey;
}

.navbar>.container-fluid {
	padding: 0px;
}

.navbar-nav {
	flex-grow: 1;
	justify-content: space-around;
}

.slide {
	position: absolute;
	width: 100%;
	height: 50px;
	top: 100%;
	background-color: #55555550;
}
</style>

<script type="text/javascript">
   $(function() {

      $(document)
            .on(
                  'click',
                  '#navbarDropdownMenuLink',
                  function() {
                     if ($(this).siblings($(".dropdown-menu")).css(
                           "display") == "none") {
                        $(this).siblings($(".dropdown-menu")).css(
                              "display", 'block')
                        for (let i = 0; i < $(".dropdown-menu").length; i++) {
                           if (($($(".dropdown-menu")[i]).text() !== $(
                                 this).siblings($(".dropdown-menu"))
                                 .text())) {
                              $($(".dropdown-menu")[i]).css(
                                    "display", "none");
                           }
                        }
                     } else {
                        $(this).siblings($(".dropdown-menu")).css(
                              "display", 'none')
                     }
                  })

      $("#submitbtn").on("click", function() {
         var speed = $("#record").val();
         var accuracy = $("#accuracy").val();
         let speedReg = /^\d{0,4}$/;
         let accuracyReg = /^\d{0,3}$/;
         let resultspeed = speedReg.test(speed);
         let resultaccuracy = accuracyReg.test(accuracy);
         if (resultspeed && resultaccuracy) {
            if (speed > 1000 || accuracy > 100) {
               alert("입력하신 정보를 다시 확인해주시기 바랍니다.");
            } else {
               $("#typingform").submit();
               return false;
            }
         } else {
            alert("입력하신 정보를 다시 확인해주시기 바랍니다.");
            return false;
         }
      })

   })
</script>

</head>
<body>
	<jsp:include page="/header.jsp" />
	<jsp:include page="/navibar.jsp" />
	<div class="container p-4 shadow bg-white rounded">
		<div class="row header">
			<h2>
				<b>타자기록</b>
			</h2>
		</div>

		<c:choose>
			<c:when test="${recentList!=null}">
				<div class="row chart">

					<c:forEach var="item" items="${recentList}">
						<div class=col-1
							style="display: table-cell; vertical-align: bottom;">
							<div class=row
								style="height: 350px; display: table-cell; vertical-align: bottom;">
								<c:choose>
									<c:when test="${item.record<350}">
										<div class=bar style="height:${item.record}px;">${item.record}</div>
									</c:when>
									<c:otherwise>
										<div class=bar style="height: 350px;">${item.record}</div>
									</c:otherwise>
								</c:choose>
							</div>
							<div class=row style="text-align: right;">${item.reg_date }</div>
						</div>
					</c:forEach>

				</div>
			</c:when>
			<c:otherwise>
			</c:otherwise>
		</c:choose>

		<form action="write.typ" method="post" id="typingform">
			<div class="row record" style="text-align: center;">

				<div class="col-8 recbox">
					<div class="row rec" style="width: 100%; height: 90%;">

						<div class="col-6">타자</div>
						<div class="col-6">정확도</div>


						<div class="col-6">
							<input type="text" name="record" id="record">
						</div>
						<div class="col-6">
							<input type="text" name="accuracy" id="accuracy">
						</div>

					</div>
				</div>

				<div class="col-4">
					<div class="row btn" style="width: 100%; height: 100%;">
						<button type="button" class="btn btn-primary" id="submitbtn"
							style="width: 100%; height: 100%;">제출</button>
					</div>
				</div>

			</div>
		</form>
		<div class="row footer"></div>
	</div>


</body>
</html>