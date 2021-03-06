package MusicPlayerUtil;

import static MusicPlayerUtil.MusicPlayerCommon.*;
import MusicPlayerService.*;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import MusicVo.Music;
import UserVo.*;
import UserService.*;

/**
 * @author 김문수
 * 2021-08-05
 * MusicPlayerServiceimpl에 있는 리스트 출력용 메서드를 클래스로 생성.
 * 메서드 여러개로 나눔.
 */

public class PageViewer {
	ArrayList<String> list = new ArrayList<String>();
	List<Music> musicList = new ArrayList<Music>();
	AsciiArt asc = new AsciiArt();
	User user = new User();
	Music music = new Music();
	MusicPlayerServiceImpl mps;
	private int printNum=10;  // 표시될 음악 갯수
	private int curPage = 1;  // 현재페이지
	private int endNum;       // 리스트를 출력하는 for문의 제한수
	private int musicNum;     // 음악 총합
	private boolean isAddMusic = false;
	private boolean isExit = false;
	private boolean isNewMyList = false; //재생목록을 새로 만들었을때 새로추가된 목록도 보이게 하기위한 플래그 입니다.
	private boolean isStart = false;
	private boolean isEnd = false;       //버튼 입력시 보이지 않는 버튼의 입력을 막기위한 플래그 입니다.
	private String myListName = "";
	
	private int messageState = INIT;
	private static final int WRONG_LIST = 1;
	private static final int NO_LIST = 2;

	public PageViewer() {}
	
	public PageViewer(MusicPlayerServiceImpl mps) {
		super();
		this.mps = mps;
	}
	
	public void message() {
		switch (messageState) {
		case WRONG_LIST:
			asc.panelArtU();
			System.out.println("		|	    	잘못된 리스트입니다.       |");
			asc.panelArtD();
			messageState = INIT;
			break;
		case NO_LIST:
			asc.panelArtU();
			System.out.println("		|       리스트내에 음악이 없습니다.    |");
			asc.panelArtD();
			messageState = INIT;
			break;
		default:
			break;
		}
	}
	
	/**
	 * 통합 출력용 메서드. 리스트를 보여주고, 선택용 버튼을 구현.
	 * @author 김문수
	 * 2021-08-06
	 */
	public void display(String listName, List<?> list) {
		if(isNewMyList) { //재생목록을 새로 만들었을때 새로운 목록도 함께 출력되게 위한 플래그 조건입니다.
			list = mps.myListKey();
			isNewMyList = false;
		}
		if(list == null) {
			messageState = WRONG_LIST;
			return;
		}
		else if(list.isEmpty()) {
			messageState = NO_LIST;
			return;
		}
		
		
		
		musicNum = list.size();
		allListViewer(listName, list);
		getPageElements(list);
		pageButton(listName, list);

		//화면에서 빠져나갈때 사용됩니다.
		if(isExit) {
			isExit = false;
			return;
		}
		else {
			curPage = 1;
			display(listName, list);
		}
	}
	/**
	 * 리스트를 출력할때 선택할 버튼들을 보여줍니다.
	 * @author 김문수
	 * 2021-08-06
	 */
	public void getPageElements(List<?> list) {
		int totalPage = getTotalPage();
		boolean prevPage = curPage > 1;
		boolean nextPage = curPage < totalPage;
		if(curPage == 1) isStart = true;
		else {
			isStart = false;
		}
		if(curPage == totalPage) isEnd = true;
		else {
			isEnd = false;
		}
		System.out.print("\n" + " Pages : " + curPage + " / " + totalPage + "\n");
		if(list.get(0) instanceof Music) {
//			return "\n" + " Pages : " + curPage + " / " + totalPage + "\n" + (prevPage ? "1.이전    " : "") + (nextPage ? "2.다음    " : "") +
//				"3.선택    4.검색    5.전체재생    0.뒤로";
			if(prevPage && !nextPage) {
				asc.prev();
			}else if(!prevPage && nextPage){
				asc.next();
			}else {
				asc.all();
			}
		}else {
			if(prevPage && !nextPage) {
				asc.prevString();
			}else if(!prevPage && nextPage){
				asc.nextString();
			}else {
				asc.allString();
			}
		}
	}
	
	public int getTotalPage() {
		return (int)Math.ceil(musicNum/(double) printNum);
	}
	/**
	 * 리스트를 불러와 번호와 함께 출력합니다 .
	 * @author 김문수
	 * 2021-08-06
	 */
	public void allListViewer(String listName, List<?> list) {
		int listNum = 0;
		int startNum = (curPage -1) * printNum;
		endNum = startNum + printNum;
		if (list.isEmpty()) {
			System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			asc.panelArtU();
			System.out.println("		|          리스트가 비었습니다.        |");
			asc.panelArtD();
			return;
		}else {
			try {
			int musicNum = list.size();
			printAttribute(listName, list);
			for (int i = startNum; i < endNum; i++) {
				if(i > musicNum) break;
				System.out.printf("%d. %s%n",listNum += 1, list.get(i));
			}
			}catch(IndexOutOfBoundsException e) {}
			System.out.println("========================================================================================================");
		}
	}
	/**
	 * 리스트가 출력되면 선택버튼을 입력받아 상호 작용 합니다 .
	 * @author 김문수
	 * 2021-08-06
	 */
	public void pageButton(String listName, List<?> list) {
		int input = nextIntFromTo("	>>>>", 0, 6);
		switch(input) {
		case 1:
			if(isStart) {
				System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
				asc.panelArtU();
				System.out.println("		|           잘못된 입력입니다.         |");
				System.out.println("		|     없는 버튼인데 누르지 마세요.     |");
				asc.panelArtD();
				isStart = false;
				return;
			}else {
				if(1 < curPage) curPage -= 1;
				display(listName, list);
				break;
			}
		case 2:
			if(isEnd) {
				System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
				asc.panelArtU();
				System.out.println("		|           잘못된 입력입니다.         |");
				System.out.println("		|     없는 버튼인데 누르지 마세요.     |");
				asc.panelArtD();
				isEnd = false;
				return;
			}else {
				if(getTotalPage() > curPage) curPage +=1;
				display(listName, list);
				break;
			}
		case 3:
			//곡선택
			choiceOne(listName, list);
			if(isAddMusic) {
				mps.addMusic(music);
				isAddMusic = false;
				isExit = true;
			}
			break;
		case 4:
			//검색
			if(list.get(0) instanceof Music) {
				mps.search(list);
			}else if(list.get(0) instanceof String) {
				mps.searchList(list);
			}
			break;
		case 5:
			if(list.get(0) instanceof Music) {
				mps.listPlay((List<Music>) list);
			}else {
				mps.createMyList();
				isNewMyList = true; //재생목록을 새로 만들었을때 새로만든 재생목록도 출력하게 하기위한 플래그 값입니다.
			}
			break;
		case 6:
			mps.deleteMyList();
		case 0:
			//종료
			isExit = true;
			isAddMusic = false;
			return;
		}
	}
	/**
	 * MyList와 Music의 속성이 달라 따로 구분해서 출력하기 위한 매서드 입니다.
	 * @author 김문수
	 * 2021-08-07
	 */
	public void printAttribute(String listName, List<?> list) {
		if(list.get(0) instanceof Music) {
//			System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			System.out.println("\n" + listName);
			System.out.println("========================================================================================================");
			System.out.println("              곡명                           가수                       앨범                장르        ");
			System.out.println("========================================================================================================");
		}else if(list.get(0) instanceof String){
//			System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			System.out.println("\n" + listName);
			System.out.println("========================================================================================================");
		}
	}
	/**
	 * MyList인지 Music인지 판단해서 각기 다른 메서드를 호출해 pageButton으로 보내줍니다. 
	 * @author 김문수
	 * 2021-08-08
	 */
	public void choiceOne(String listName, List<?> list) {
		int input;
		if(list.get(0) instanceof Music) {
			System.out.println("┌──────────────────────────────────────────────────────────┐"); 
			System.out.println("┃            음악의 번호를 고르세요.    ( 뒤로 > 0 )       ┃");
			System.out.println("└──────────────────────────────────────────────────────────┘");
			input = nextIntFromTo("		>>>> ", 0, endNum);
			if(input == 0) {
				return;
			}else {
			music = (Music) list.get(curPage * input -1);
			mps.pickMusic(music);
			}
		}
		else if(list.get(0) instanceof String) {
			mps.pickList(choiceMyList(list));
		}
	}
	/**
	 * MyList용 리스트뷰어 메서드입니다. 
	 * @author 김문수
	 * 2021-08-08
	 * @return 
	 */
	public String choiceMyList(List<?> list) {
		System.out.println("┌──────────────────────────────────────────────────────────┐"); 
		System.out.println("┃                마이리스트의 번호를 고르세요.             ┃");
		System.out.println("└──────────────────────────────────────────────────────────┘");
		int input = nextIntFromTo("		>>>> ", 1, endNum);
		myListName = (String) list.get(curPage * input -1);
		return myListName;
	}

	public int getPrintNum() {
		return printNum;
	}

	public void setPrintNum(int printNum) {
		this.printNum = printNum;
	}

	public int getMusicNum() {
		return musicNum;
	}

	public void setMusicNum(int musicNum) {
		this.musicNum = musicNum;
	}

	public int getCurPage() {
		return curPage;
	}

	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}
	
	public boolean isAddMusic() {
		return isAddMusic;
	}

	public void setAddMusic(boolean isAddMusic) {
		this.isAddMusic = isAddMusic;
	}

	public String getMyListName() {
		return myListName;
	}

	public void setMyListName(String myListName) {
		this.myListName = myListName;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	public boolean isEnd() {
		return isEnd;
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}
	
}
