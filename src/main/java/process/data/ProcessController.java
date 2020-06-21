package process.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import books.data.BooksDto;
import books.data.BooksServiceInter;
import files.data.FilesDto;
import files.data.ProcessFilesDto;
import member.data.MemberDto;
import upload.util.ManageFileClass;
import upload.util.ReadBooksList;
import upload.util.WebCrawling;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


@RestController
@CrossOrigin
public class ProcessController {
	
	@Autowired
	private ProcessServiceInter service;
	
	@Autowired
	private BooksServiceInter booksService;
	
	
	@GetMapping("/process/searchTeacher")
	public List<MemberDto> searchTeacher()
	{
	
		System.out.println("react >> searchTeacher");
		
		return service.searchTeacher();
	}
	
	@GetMapping("/process/getBooksList")
	public List<BooksDto> getBooksList()
	{
		System.out.println("react >> getBooksList");
		List<List<String>> bookslist = new ReadBooksList().readBooks();
		
		List<BooksDto> list = new ArrayList<BooksDto>();
		
		for(List<String> s : bookslist)
		{
			BooksDto dto = new BooksDto();
			for(int i=0; i<s.size(); i++)
			{
				dto.setBooks_brand(s.get(1));
				dto.setBooks_name(s.get(4));
				dto.setBooks_writer(s.get(5));
				
			}
			list.add(dto);
		}
		
		
		return list;
	}
		
	@RequestMapping(value="/process/insert",method=RequestMethod.POST)
	public int insertProcess(@ModelAttribute ProcessDto processdto,HttpServletRequest request)
	{
		
		System.out.println("react >> processInsert");
		String teachernum = processdto.getProcess_teacher();
		
		MemberDto teacher = service.selectOneTeacher(teachernum);
		
		processdto.setProcess_teachername(teacher.getMember_name());
		processdto.setProcess_member_num(12);
		
		service.insertProcess(processdto);
		System.out.println("insert ����");
		int maxnum = service.selectProcessMaxnum();	
	
		
		if(processdto.getProcess_uploadfiles()!=null) {			
			ManageFileClass mfc = new ManageFileClass();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String path = request.getSession().getServletContext().getRealPath("/WEB-INF/uploadfile");
			System.out.println("path:"+path);
			String nowdate = sdf.format(new Date());
			
			for(MultipartFile uploadfile : processdto.getProcess_uploadfiles())
			{
				String fileName= nowdate+"_"+maxnum+"_"+uploadfile.getOriginalFilename();
				mfc.fileUpload(fileName,uploadfile, path, maxnum);
				ProcessFilesDto processfilesdto = new ProcessFilesDto();
				
				processfilesdto.setProcessfiles_process_num(maxnum);
				processfilesdto.setProcessfiles_process_filename(fileName);
				service.insertProcessFiles(processfilesdto);
			}
			
			
		}
			
		return maxnum;
	}
	
	@RequestMapping(value="/process/list",method=RequestMethod.POST)
	public List<ProcessListDto> getAllProcess(){
		System.out.println("react >> getAllProcess");
		
		return service.getAllProcess();
		
	}
	@RequestMapping(value="/process/detail",method=RequestMethod.GET)
	public Map<String,Object> selectOneProcess(@RequestParam int process_num)
	{
		System.out.println("react >> process/detail");
		Map<String,Object> process = new HashMap<String,Object>();
		
		ProcessDto processdto = service.selectOneProcess(process_num);
		List<ProcessFilesDto> processfiles = service.processFilesList(process_num);
		List<BooksDto> books = booksService.processBooks(process_num);
		
		System.out.println("processdto_subject : "+processdto.getProcess_subject() );
		System.out.println("files size : " + processfiles.size());
		System.out.println("books size : " + books.size());
		
		process.put("processdto",processdto);
		process.put("processfiles",processfiles);
		process.put("books",books);
		
		return process;
	}
	
	@RequestMapping(value="/process/getImages",method=RequestMethod.GET)
	public List<String> getBooksImages(int num)
	{
		List<BooksDto> s = booksService.processBooks(num);
		
		WebCrawling craw = new WebCrawling();
	
		List<String> booksImages = craw.booksImageCrawling(s);
		
		if(booksImages.size() > 0) {
			return booksImages;		
		}
		
		return null;
	}
	
	//ä����� ũ�Ѹ� �޼���
	@GetMapping("/process/hirelist")
	public Map<String,List<String>> getHireList(String searchtxt){
		
		Map<String,List<String>> hirelist = new HashMap<String,List<String>>();
		
		WebCrawling craw = new WebCrawling();
		
		hirelist = craw.getHireList(searchtxt);
		
		
		return hirelist;
	}
	
}