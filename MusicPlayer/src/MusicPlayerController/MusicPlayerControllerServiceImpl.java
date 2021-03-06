package MusicPlayerController;

import static MusicPlayerUtil.MusicPlayerCommon.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import MusicPlayerUtil.PageViewer;
import MusicVo.Music;
import UserService.AsciiArt;

/**
 * 뮤직플레이어 컨트롤러 클래스를 이용한 다양한 뮤직컨트롤 메서드를 제공합니다
 * @author 문현석
 * 2021-08-02
 *
 */
public class MusicPlayerControllerServiceImpl extends Thread implements MusicPlayerControllerService{
	
	private MusicPlayerController mpc = new MusicPlayerController();
	private List<Music> playList = new ArrayList<>();
	private AsciiArt asc = new AsciiArt();
	private PageViewer pv = null;
	
	private int playIndex 		= 0; 			// 현재 재생순번을 기록하는 변수
	private int volGain			= 5;			// 볼륨 조절 값입니다.
	private Thread thread       = null;
	private boolean isLoad		= false;		// 쓰레드를 이용하여 다음곡 로드를 할것인지 선택하는 flag
	private boolean isRun		= true;			// 쓰레드를 정지시킬때 false값을 가지게 합니다.
	private boolean isLastSong	= false;		// 리스트끝까지 플레이 됐을때 true값을 가제 됩니다.
	private boolean isSetVoluem = false;		// 볼륨 조절을 했을때 디스플레이에 출력되게 하기위한 flag
	private boolean isGuest		= false;		// 게스트인 경우 음악재생은 1분으로 제한하기위한 flag

	private int playState				= ALL_ONCE;
	private static final int ONCE 		= 0;	// 한곡만 재생
	private static final int ONCE_LOOP 	= 1;	// 한곡만 반복재생
	private static final int ALL_ONCE 	= 2;	// 전체곡 한번만 재생	//*기본값
	private static final int ALL_LOOP	= 3;	// 전체곡 반복재생
	
	private int messageState				= INIT;
	private static final int NO_LIST_ERROR 	= 1;
	
	public MusicPlayerControllerServiceImpl() {}
	
	public MusicPlayerControllerServiceImpl(PageViewer pv){
		this.pv = pv;
	}
	
	/**
	 * 외부에서 뮤직리스트를 이용해 생성할때 플레이인덱스 또한 0으로 초기화 해줍니다.
	 * 또한기존리스트의 인스턴스와 연결을 끊습니다.
	 * @author 문현석
	 * 2021-08-03
	 */
	public void setPlayList(List<Music> playList) {
		this.playList = null;
		this.playList = new ArrayList<>(playList);
		this.playIndex = 0;
	}
	
	public List<Music> getPlayList() {
		return this.playList;
	}
	
	
	public boolean isGuest() {
		return isGuest;
	}

	/**
	 * 현재 프로그램 이용자가 게스트인지 아닌지 알려주기위한 setter입니다.
	 * @author 문현석
	 * 2021-08-11
	 */
	public void setGuest(boolean isGuest) {
		this.isGuest = isGuest;
	}
	
	/**
	 * 외부에서 선택한곡을 바로 재생하길 원할때 호출되는 메서드입니다.
	 */
	public void instantPlay(Music music) { 
		//현재 리스트가 비어있으면 리스트에 추가하고 바로 재생
		if(playList.isEmpty()) { 
			playList.add(music);
			mpc.setState(STARTED);
			playSequence(NOTHING,false);
		}
		//현재 리스트에 노래가있을경우, 바로 재생할 music을 리스트 다음 인덱스에 추가하고
		//플레이인덱스를 증가시켜 방금 추가한 노래를 바로 재생시킴.
		else {
			//다음 인덱스가 범위를 벗어난 경우 바로 추가하고 재생시킨다.
			int nextIndex = playIndex + 1;
			if(nextIndex >= playList.size()) {
				playList.add(music);
			}
			else {
				playList.add(nextIndex, music);
			}
			mpc.setState(STARTED);
			playSequence(NEXT, true);
		}
		if(mpc.getThread() == null) mpc.start();
		if(thread == null) start();
	}

	/**
	 * 파라메터로받은 리스트 전체를 재생합니다.
	 * @author 문현석
	 * 2021-08-09
	 */
	public void listPlay(List<Music> musicList) {
		setPlayList(musicList);
		playSequence(NOTHING, true);
		mpc.setState(STARTED);
		
		if(mpc.getThread() == null) mpc.start();
		if(thread == null) start();
	}
	
	/**
	 * 현재 재생목록에 노래를 추가하는 메서드(외부에서 사용됩니다)
	 * @author 문현석
	 * 2021-08-11
	 */
	public void addNowPlayList(Music music) {
		playList.add(music);
	}
	
	/**
	 * 입력된 키값에 따라 컨트롤러를 조작합니다.
	 * synchronized 하지 않으면 수많은 버그초래
	 * @author 문현석
	 * 2021-08-03
	 */
	public void musicController() {
		if(playList == null) {
			messageState = NO_LIST_ERROR;
			return;
		}
		else if(playList.isEmpty()){
			messageState = NO_LIST_ERROR;
			return;
		}
		
		if(mpc.getThread() == null) mpc.start();
		if(thread == null) start();
		
		while(true) {
			playInfoView(); //컨트롤러 출력부분 메서드
			int input = nextIntFromTo("    >>>>>>>>  ", 0, 8);
			switch (input) {
			case 1: //.재생
				play(mpc.getState());
				break;
			case 2: //.정지
				mpc.setState(STOPPED);
				System.out.println("정지");
				break;
			case 3: //.리스트의 이전곡 선택 공통된 코드를 따로 만들것
				playSequence(PREV,true);
				break;
			case 4: //.리스트의 다음곡 선택
				playSequence(NEXT,true);
				break;
			case 5: //. 재생환경 설정
				playSettings();
				break;
			case 6: //. 볼륨 증가 
				//볼륨범위 백분율에 대한 1%값이 0.86이므로 volGain(int형)값을 통해
				//조절 해야 백분율 출력시 의도한 조절 값에따른 결과를 얻을수 있습니다.
				// ex) volGain == 2일때 볼륨값을 백분율하여 출력시 보여지는 값또한 2증가한 값을 보여줄수있음 
				setVolume((float)(-volGain * 0.86));
				isSetVoluem = true;
				break;
			case 7: //. 볼륨 감소
				setVolume((float)(volGain * 0.86));
				isSetVoluem = true;
				break;
			case 8: //. 현재 재생목록 보기
				this.pv.display("♩♪ Now PlayList ♬♩", playList);
				break;
			case 0: 
				return;
			default:
				break;
			}
		}
	}
	
	/**
	 * 뮤직컨트롤 쓰레드의 재생상태에따라서 재생할지 일시정지 할지 결정합니다.
	 * @author 문현석
	 * 2021-08-04
	 * @param musicState
	 */
	private void play(int musicState) {
		if(musicState == INIT) {
			mpc.setState(STARTED);
			playSequence(NOTHING,false);
		}
		else if(musicState == STARTED) {
			mpc.setState(SUSPENDED);
		}
		else if(musicState == SUSPENDED) {
			mpc.setState(STARTED);
		}
		else{
			mpc.setState(STARTED);
		}
	}
	
	/**
	 * 다음곡, 이전곡 기능을 구현한 메서드입니다.
	 * @author 문현석
	 * 2021-08-08(수정)파라메터 변경
	 * @param sequence (다음곡, 이전곡에 대한 상수(NEXT, PREV, STAY)를 인자로 받습니다.)
	 * @param isSkip (메서드가 호출됐을때 현재 재생중인 곡을 스킵할것인지 말것인지 선택합니다)
	 */
	private void playSequence(int sequence,boolean isSkip) {
		if(sequence == NEXT) playIndex++;
		else if(sequence == PREV) playIndex--;
		
		// 플레이 인덱스가 범위를 넘어갔을때 안전하게 처리 합니다.
		safeIndexing();
		
		// 디코딩 도중 사용자가 다음곡,이전곡 으로 전환할시 디코딩을 스킵한다.
		if(mpc.isDecoding()) mpc.setDecodeSkip(true);
		
		//재생도중 곡을 변경하면 재생로직을 빠져나간다.
		mpc.setSkip(isSkip); 	
		
		//재생중이던 곡의 재생관련 데이터를 초기화 한다.
		mpc.close();	
		
		//playIndex에 해당하는 곡을 로드합니다.
		isLoad = true;
	}
	
	/**
	 * 뮤직리스트의 인덱스가 범위를 넘어갔을때 안전하게 처리함.
	 */
	private void safeIndexing() {
		if(playList == null) return;
		if(playIndex < 0 ) {
			playIndex = playList.size() -1;
		}
		else if(playIndex >= playList.size()) { 
			playIndex = 0;
		}
	}
	
	/**
	 * 멀티쓰레드를 이용해서 파일을 로드하는 동시에 메인쓰레드는 정보를 출력하게했음.
	 * @author 문현석
	 * 2021-08-07
	 * @param musicState
	 */
	private void preLoad() {
		if(!isLoad) return;
		mpc.open(playList.get(playIndex).getPath());
		
		if(this.mpc.getState() == FINISHED) this.mpc.setState(STARTED);
		
		isLoad = false;
	}
	
	/**
	 * 한곡이 끝났을때 재생환경설정 값에따라 
	 * 재생설정이 바뀌는 부분을 담당하는 메서드입니다.
	 * @author 문현석
	 * 2021-08-05
	 */
	private void autoPlay() {
		//현재곡 다음 인덱스가 리스트 사이즈와 같을때 현재곡이 마지막곡인것을 알수있음
		if(playIndex + 1 >= playList.size()) isLastSong = true;
		
		//게스트 유저가 이용시 재생 시간을 1분으로 제한합니다.
		if(isGuest) {
			if(mpc.getPosition()/1000 > 60) {
				mpc.setState(FINISHED);
			}
		}
		
		// 한곡이 끝났을때 재생환경설정값에 따라 switch문이 작동합니다.
		if(mpc.getState() == FINISHED) {
			
			switch (playState) {
			case ONCE: 		//한곡만 한번 재생일때 더이상재생 시키지 않음.
				mpc.setState(INIT);
				break;
				
			case ONCE_LOOP:	//한곡만 반복 재생일때 
				playSequence(NOTHING,false);
				break;
				
			case ALL_ONCE:	//리스트 전체 한번만 재생
				if(isLastSong) { 
					//리스트 끝까지 재생됐을때 정지 시킵니다.
					mpc.setState(INIT);
					isLastSong = false;
				}
				else { 
					//리스트 끝이 아닌경우 다음곡을 재생시킵니다.
					playSequence(NEXT, false);
				}
				break;
				
			case ALL_LOOP: // 리스트 전체 반복재생일때 계속해서 다음곡을 재생시킵니다.
				playSequence(NEXT, false);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * 재생환경 설정에관한 메서드입니다.
	 * @author 문현석
	 * 2021-08-10
	 */
	private void playSettings() {
		int input = ALL_ONCE;
		String message = "";
		
		while(true) {
			System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			asc.consoleUp();
			System.out.println(message);
			asc.consoleDown();
			System.out.println("┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐");
			System.out.println("┃ 1.한곡 한번재생 ┃ 2.한곡 반복재생 ┃ 3.리스트 한번재생 ┃ 4.리스트 반복재생 ┃ 5.재생목록 셔플 ┃ 0.뒤로 ┃");
			System.out.println("└──────────────────────────────────────────────────────────────────────────────────────────────────────┘");
			input = nextIntFromTo("    >>>>>>>>>>>>>  ", 0, 5);
			switch (input) {
			case 1:
				playState = ONCE;
				message = "한곡 한번재생 설정되었습니다.";
				break;
			case 2:
				playState = ONCE_LOOP;
				message = "한곡 반복재생 설정되었습니다.";
				break;
			case 3:
				playState = ALL_ONCE;
				message = "리스트 한번재생 설정되었습니다.";
				break;
			case 4:
				playState = ALL_LOOP;
				message = "리스트 반복재생 설정되었습니다.";
				break;
			case 5:
				//재생목록 셔플하는 기능추가
				message = "재생목록 순서를 섞었습니다.";
				Collections.shuffle(playList);
				break;
			case 0:
				//메서드를 빠져나감
				return;
			default:
				break;
			}
		}
	}
	
	/**
	 * 볼륨을 조절하는 메서드입니다.
	 * @author 문현석
	 * 2021-08-10
	 */
	private void setVolume(float gain) {
		mpc.setVolume(gain);
	}
	
	/**
	 * 컨트롤러의 뷰어부분입니다.
	 * @author 문현석
	 * 2021-08-05
	 */
	private void playInfoView() {
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		//MsicPlayerContorller클래스의 오류 또는 특정상황에 대한 메세지를 출력
		mpc.message();
		
		//비회원이 컨트롤러에 진입했을 경우 나오는 안내문구입니다.
		if(isGuest) System.out.println("\n비회원은 1분 미리듣기만 가능합니다.\n");
		
		asc.consoleUp();
		//다음 플레이 인덱스가 리스트 끝인경우 출력
		if( playIndex + 1 >= playList.size()) {
			System.out.println("\n다음 곡명 : 리스트 마지막 입니다.\n");
		}
		//다음곡명을 출력
		else {
			System.out.println("\n다음 곡명 : " + playList.get(playIndex + 1).getTag(TITLE) + "\n");
		}
		
		//현재곡명을 화면에 출력하고 볼륨 조절시 볼륨도 화면에 출력됩니다.
		System.out.println("현재 곡명 : " + playList.get(playIndex).getTag(TITLE) + "\n");
		asc.consoleDown();
		
		//볼륨 조절시 화면에 나타나게 됩니다.
		String volumeStr = "";
		if(isSetVoluem) {
			String str = String.format("< Volume: %-3s> ", mpc.getVolume());
			volumeStr = str;
			isSetVoluem = false;
		}
		else {
			volumeStr = "               ";
		}
		
		//버튼 출력문입니다.
		System.out.println("┌────────────────────────────────────────────────────────────────────────────────┐");
		System.out.println("┃  1.재생/일시정지   2.정지    3.이전곡    4.다음곡                              ┃");
		System.out.println("┃                           " + volumeStr+"                                      ┃");
		System.out.println("┃  5.재생환경설정    6.볼륨 다운    7.볼륨 업   8.현재 재생목록 보기   0.뒤로    ┃");
		System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
	}
	
	/**
	 * 왜 this의 new Thread로 생성하지 않으면 interrupt가 작동하지 않을까? 
	 * -> 잘못 이해했다 쓰레드의 interrupt는 즉시 실행되는게 아니라 쓰레드가
	 * 일시 정지 상태가 되면  interruptException이 발생하고 이때 interrupt가 실행된다. 
	 * @author 문현석
	 * 2021-08-05
	 */
	public void start() {
		synchronized(this) {
			if(thread == null) {
				thread = new Thread(this);
			}
			thread.start();
		}
	}
	
	/**
	 * autoPlay와 preLoad메서드를 멀티쓰레드로 작동시킴
	 * 이유: 출력부분과 메인쓰레이드에서 실행되는 로직과 별개로 작동시키기 위해서
	 * @author 문현석
	 * 2021-08-05
	 */
	public void run() {
		while(thread.isAlive()) { //while문의 조건으로 isRun을 줘버렸는데 쓰레드가 일을 안한다.. 뭐지? 
			if(!isRun) break;
			autoPlay();
			safeIndexing(); //preLoad보다 우선순위가 빨라야 함.
			preLoad();
		}
		isRun = true;
		thread = null;
	}
	
	/**
	 * 외부에서 쓰레드를 종료시킬때 호출하는 메서드입니다.
	 * 재생관련 멤버를 모두 초기화 해줍니다.
	 * @author 문현석
	 * 2021-08-06
	 */
	public void closeThread() {
		isRun = false;
		playList.clear();
		playIndex = 0;
		mpc.setState(INIT);
		mpc.closeThread();
	}

	public void message() {
		switch (messageState) {
		case NO_LIST_ERROR:
			System.out.println("재생할 리스트가 없습니다.");
			break;

		default:
			break;
		}
	}

	@Override
	public void setVolumeValue(int vol) {
		// TODO Auto-generated method stub
		this.volGain = vol;
	}
}
