package controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import config.AssConfig;
import config.AssFileConfig;
import dao.AssDAO;
import dao.AssFilesDAO;
import dao.AssSubmitDAO;
import dao.MemberDAO;
import dto.AssDTO;
import dto.AssFilesDTO;
import dto.AssSubmitDTO;




@WebServlet("*.ass")
public class AssController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Logger l = Logger.getLogger(AssController.class); //import org.apache.log4j.Logger




	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");

		String requestURI = request.getRequestURI();
		String ctxPath = request.getContextPath();
		String url = requestURI.substring(ctxPath.length());

		AssDAO dao = AssDAO.getInstance();
		AssFilesDAO daoF = AssFilesDAO.getInstance();
		AssSubmitDAO daoS = AssSubmitDAO.getInstance();
		MemberDAO daoM = MemberDAO.getInstance();

		try {
			if(url.contentEquals("/writeForm.ass")) {

				response.sendRedirect("kh/assignment/assWrite.jsp");

			}else if(url.contentEquals("/write.ass")) {

				System.out.println("write.ass");

				String email = (String) request.getSession().getAttribute("login");

				String folderFilesPath = request.getServletContext().getRealPath("assFiles");
				File folderfilesFolder = new File(folderFilesPath); //java.io.file

				if(!folderfilesFolder.exists()) {
					folderfilesFolder.mkdir(); //make directory
				}

				String filesPath = request.getServletContext().getRealPath("assFiles/"+email);
				File filesFolder = new File(filesPath); 
				if(!filesFolder.exists()) {
					filesFolder.mkdir(); //make directory
				}
				System.out.println("프로젝트가 저장된 진짜 경로: " + filesPath);
				int maxSize = AssFileConfig.uploadMaxSize; //10메가

				MultipartRequest multi = new MultipartRequest(request, filesPath, maxSize, "utf-8", new DefaultFileRenamePolicy());
				//파라미터: 멀티파트로 업그레이드할 인자, 저장 경로, 최대 사이즈 , 인코딩, 파일명명규칙(겹치는 이름있으면 뒤에 숫자붙임. 인터넷 검색 필요)

				int seq = dao.getSeq();
				System.out.println("board_seq: "+seq);
				String title = multi.getParameter("title");
				String contents = multi.getParameter("contents");
				

				String writer = email;
				String id = daoM.getAllInfo(email).getId();
				String khClass = daoM.getAllInfo(email).getKhClass();
				String branch = daoM.getAllInfo(email).getBranch();
				int viewCount = 0;



				AssDTO dto = new AssDTO(seq, writer, id, title, contents, khClass, branch, null, viewCount);

				int insert = dao.insert(dto);
				if(insert>0) {
					System.out.println("글쓰기 입력 완료");
				}


				String oriName = multi.getOriginalFileName("file"); //오리지널 이름
				String sysName = multi.getFilesystemName("file"); //서버에 저장된 이름
				System.out.println(oriName);
				System.out.println(sysName);

				if(oriName !=null) {
					AssFilesDTO dtoF = new AssFilesDTO(0, oriName, sysName, null, seq);
					int result = daoF.insert(dtoF);
					if(result>0) {
						System.out.println("파일 업로드 완료");
					}else {
						System.out.println("파일 업로드 안 됨.");
					}
				}
				l.trace(request.getRemoteAddr()+" 과제게시판 작성");

				response.sendRedirect("list.ass?currentPage=1");

			}else if(url.contentEquals("/list.ass")) {

				String email = (String)request.getSession().getAttribute("login");

				String id = daoM.getAllInfo(email).getId();
				String khClass= daoM.getAllInfo(email).getKhClass();
				String branch = daoM.getAllInfo(email).getBranch();
				String position = daoM.getAllInfo(email).getPosition();


				String category = request.getParameter("category");
				String keyword = request.getParameter("keyword");


				int currentPage = Integer.parseInt(request.getParameter("currentPage"));
				System.out.println("현재 페이지: " +currentPage);

				int endNum= currentPage * AssConfig.recordCountPage;
				int startNum = endNum - (AssConfig.recordCountPage-1);

				List<AssDTO> assList;
				List<String> pageNavi;

				if(keyword!=null && category!=null) {
					keyword = keyword.toLowerCase();
					System.out.println("검색어: " + keyword);
					assList = dao.getPageList(khClass, branch, startNum, endNum, category, keyword);
					pageNavi = dao.getPageNavi(khClass, branch, currentPage,category,keyword);
				}else {
					assList = dao.getPageList(khClass, branch, startNum, endNum, null, null);
					pageNavi = dao.getPageNavi(khClass, branch, currentPage,null,null);
				}


				request.setAttribute("loginId", id);
				request.setAttribute("loginPosition", position);
				request.setAttribute("assList", assList);

				request.setAttribute("pageNavi", pageNavi);
				request.setAttribute("currentPage", currentPage);
				request.setAttribute("category", category);
				request.setAttribute("keyword", keyword);

				RequestDispatcher rd = request.getRequestDispatcher("kh/assignment/assList.jsp");
				rd.forward(request,response);

			}else if(url.contentEquals("/view.ass")) {

				int seq = Integer.parseInt(request.getParameter("ass_seq"));
				String email = (String)request.getSession().getAttribute("login");
				String id = daoM.getAllInfo(email).getId();

				AssDTO ass1  = dao.select(seq); //해당 seq의 정보들 추출
				int viewCount = ass1.getViewCount();
				System.out.println(viewCount);
				int result = dao.addViewCount(seq, viewCount);
				if(result>0) {
					System.out.println("++viewCount");
				}

				AssDTO ass2 = dao.select(seq); //viewCount가 1 올라간 내역으로 다시 추출.
				AssFilesDTO assFiles = daoF.select(seq);

				List<AssSubmitDTO> assSubmit = daoS.selectAll(seq);

				request.setAttribute("loginId", id);
				request.setAttribute("assView", ass2);
				request.setAttribute("assFiles", assFiles);
				request.setAttribute("assSubmit", assSubmit);


				RequestDispatcher rd = request.getRequestDispatcher("kh/assignment/assView.jsp");
				rd.forward(request,response);


			}else if(url.contentEquals("/delete.ass")) {

				System.out.println("과제 게시물 삭제 요청");

				int delSeq = Integer.parseInt(request.getParameter("delSeq"));
				String email = dao.select(delSeq).getWriter();

				System.out.println("지울 과제 seq: "+delSeq);
				System.out.println("지울 파일의 parent: "+ delSeq);
				System.out.println("writer: "+email);

				String filesPath = request.getServletContext().getRealPath("assFiles/"+email);
				String sysName = daoF.getSysName(delSeq);

				if(sysName!=null) {
					File targetFile = new File(filesPath +"/" + sysName); 

					boolean result = targetFile.delete();
					System.out.println("파일 삭제 여부: " + result);

					daoF.deleteAll(delSeq);
				}


				int deleteAss = dao.delete(delSeq);
				if(deleteAss>0) {
					System.out.println("과제 게시물 삭제 성공!");
				}else {
					System.out.println("과제 게시물 삭제 실패");
				}

				l.trace(request.getRemoteAddr()+" 과제게시판 삭제");
				response.sendRedirect("list.ass?currentPage=1");





			}else if(url.contentEquals("/modiForm.ass")) {

				System.out.println("modiForm.ass");

				int seq = Integer.parseInt(request.getParameter("ass_seq"));

				AssDTO ass  = dao.select(seq);
				AssFilesDTO assFiles = daoF.select(seq);

				request.setAttribute("assView", ass);
				request.setAttribute("assFiles", assFiles);

				RequestDispatcher rd = request.getRequestDispatcher("kh/assignment/assModify.jsp");
				rd.forward(request,response);


			}else if(url.contentEquals("/modiProc.ass")) {

				System.out.println("과제 수정 요청");

				String email = (String) request.getSession().getAttribute("login");

				String folderFilesPath = request.getServletContext().getRealPath("assFiles");
				File folderfilesFolder = new File(folderFilesPath); //java.io.file

				if(!folderfilesFolder.exists()) {
					folderfilesFolder.mkdir(); //make directory
				}

				String filesPath = request.getServletContext().getRealPath("assFiles/"+email);
				File filesFolder = new File(filesPath); 
				if(!filesFolder.exists()) {
					filesFolder.mkdir(); //make directory
				}
				System.out.println("프로젝트가 저장된 진짜 경로: " + filesPath);
				int maxSize = AssFileConfig.uploadMaxSize; //10메가

				MultipartRequest multi = new MultipartRequest(request, filesPath, maxSize, "utf-8", new DefaultFileRenamePolicy());
				//파라미터: 멀티파트로 업그레이드할 인자, 저장 경로, 최대 사이즈 , 인코딩, 파일명명규칙(겹치는 이름있으면 뒤에 숫자붙임. 인터넷 검색 필요)

				int seq = Integer.parseInt(request.getParameter("ass_seq"));
				System.out.println("ass_seq: "+seq);
				String title = multi.getParameter("title");
				String contents = multi.getParameter("contents");
				

				String writer = email;
				String id = daoM.getAllInfo(email).getId();
				String khClass = daoM.getAllInfo(email).getKhClass();
				String branch = daoM.getAllInfo(email).getBranch();



				AssDTO dto = new AssDTO(seq, writer, id, title, contents, khClass, branch, null, 0);

				int insert = dao.update(dto);
				if(insert>0) {
					System.out.println("글 수정 완료");
				}


				String delTarget = multi.getParameter("delete");


				if(delTarget!= null) {  

					System.out.println("지울 파일 번호 "+ delTarget);

					String sysName = daoF.getSysName(Integer.parseInt(delTarget));
					File targetFile = new File(filesPath +"/" + sysName); 
					boolean result = targetFile.delete();
					System.out.println("파일 삭제 여부: " + result);
					if(result) {daoF.delete(Integer.parseInt(delTarget));}

				}								


				String oriName = multi.getOriginalFileName("file"); //오리지널 이름
				String sysName = multi.getFilesystemName("file"); //서버에 저장된 이름
				System.out.println(oriName);
				System.out.println(sysName);

				AssFilesDTO dtoF = new AssFilesDTO(0, oriName, sysName, null, seq);
				int result = daoF.insert(dtoF);
				if(result>0) {
					System.out.println("파일 업데이트 완료");
				}else {
					System.out.println("파일 업로드 안 됨.");
				}

				l.trace(request.getRemoteAddr()+" 과제게시판 수정");
				response.sendRedirect("view.ass?ass_seq="+seq);


			}else if(url.contentEquals("/download.ass")) {


				String email = request.getParameter("writer");
				String oriName = request.getParameter("oriName");
				String sysName = request.getParameter("sysName");

				String filesPath = request.getServletContext().getRealPath("assFiles/"+email);

				File targetFile = new File(filesPath+"/"+sysName); //import io.File


				try(
						FileInputStream fis = new FileInputStream(targetFile);
						DataInputStream dis = new DataInputStream(fis);
						DataOutputStream dos = new DataOutputStream(response.getOutputStream());
						){

					byte[] fileContents = new byte[(int)targetFile.length()];
					dis.readFully(fileContents);
					oriName = new String(oriName.getBytes("utf-8"), "iso-8859-1");
					//파일 이름의 인코딩 방식을 크롬이 인식할 수 있는 iso-8859-1로 수정

					//1. 하드디스크에 있는 파일의 내용을 램으로 복사

					response.reset(); //Response 객체의 기본 동작을 모두 제거
					response.setContentType("application/octet-stream"); 
					//8진수 stream(binary 내용)을 전송하는 것이라는 것을 명시
					response.setHeader("content-disposition", "attachment; filename=\""+oriName+"\"");
					//attachment: 첨부파일을 전송하는 것을 명시, 다운로드 하라는 지침
					//client에 전송해야 하는 파일명은 클라이언트가 업로드한 이름이어야 하므로 oriName

					dos.write(fileContents);
					dos.flush();
				}

			}


		}catch(Exception e) {
			e.printStackTrace();
			response.sendRedirect("error.jsp");
		}
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
